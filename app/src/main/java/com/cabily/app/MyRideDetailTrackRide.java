package com.cabily.app;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cabily.pojo.CancelTripPojo;
import com.cabily.subclass.ActivitySubClass;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mylibrary.dialog.PkDialog;
import com.mylibrary.googlemapdrawpolyline.GMapV2GetRouteDirection;
import com.mylibrary.gps.GPSTracker;
import com.mylibrary.widgets.RoundedImageView;
import com.mylibrary.xmpp.ChatService;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Prem Kumar and Anitha on 11/20/2015.
 */
public class MyRideDetailTrackRide extends ActivitySubClass implements View.OnClickListener {
    private TextView tv_done;
    private TextView tv_drivername, tv_carModel, tv_carNo, tv_rating, tv_time, tv_timeMinute;
    private RoundedImageView driver_image;
    private RelativeLayout rl_callDriver, rl_endTrip;
    private GoogleMap googleMap;
    MarkerOptions marker;
    GPSTracker gps;
    private double MyCurrent_lat = 0.0, MyCurrent_long = 0.0;

    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private String driverID = "", driverName = "", driverImage = "", driverRating = "",
            driverLat = "", driverLong = "", driverTime = "", rideID = "", driverMobile = "",
            driverCar_no = "", driverCar_model = "", userLat = "", userLong = "";

    private boolean isReasonAvailable = false;
    Dialog dialog;

    private SessionManager session;
    private String UserID = "";
    ArrayList<CancelTripPojo> itemlist_reason;

    GMapV2GetRouteDirection v2GetRouteDirection;
    Document document;
    LatLng fromPosition;
    LatLng toPosition;
    MarkerOptions markerOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myride_detail_track_ride);
        initialize();
        initializeMap();

        //Start XMPP Chat Service
        ChatService.startUserAction(MyRideDetailTrackRide.this);

    }

    private void initialize() {
        cd = new ConnectionDetector(MyRideDetailTrackRide.this);
        isInternetPresent = cd.isConnectingToInternet();
        gps = new GPSTracker(MyRideDetailTrackRide.this);
        session = new SessionManager(MyRideDetailTrackRide.this);
        itemlist_reason = new ArrayList<CancelTripPojo>();
        v2GetRouteDirection = new GMapV2GetRouteDirection();
        markerOptions = new MarkerOptions();

        tv_done = (TextView) findViewById(R.id.myride_detail_track_your_ride_done_textview);
        tv_drivername = (TextView) findViewById(R.id.myride_detail_track_your_ride_driver_name);
        tv_carModel = (TextView) findViewById(R.id.myride_detail_track_your_ride_driver_carmodel);
        tv_carNo = (TextView) findViewById(R.id.myride_detail_track_your_ride_driver_carNo);
        tv_rating = (TextView) findViewById(R.id.myride_detail_track_your_ride_driver_rating);
        tv_time = (TextView) findViewById(R.id.myride_detail_track_your_ride_arrive_time_textview);
        tv_timeMinute = (TextView) findViewById(R.id.myride_detail_track_your_ride_arrive_minute_textview);
        driver_image = (RoundedImageView) findViewById(R.id.myride_detail_track_your_ride_driverimage);
        rl_callDriver = (RelativeLayout) findViewById(R.id.myride_detail_track_your_ride_calldriver_layout);
        rl_endTrip = (RelativeLayout) findViewById(R.id.myride_detail_track_your_ride_endtrip_layout);

        Intent intent = getIntent();
        if (intent != null) {
            driverID = intent.getStringExtra("driverID");
            driverName = intent.getStringExtra("driverName");
            driverImage = intent.getStringExtra("driverImage");
            driverRating = intent.getStringExtra("driverRating");
            driverLat = intent.getStringExtra("driverLat");
            driverLong = intent.getStringExtra("driverLong");
            driverTime = intent.getStringExtra("driverTime");
            rideID = intent.getStringExtra("rideID");
            driverMobile = intent.getStringExtra("driverMobile");
            driverCar_no = intent.getStringExtra("driverCar_no");
            driverCar_model = intent.getStringExtra("driverCar_model");
            userLat = intent.getStringExtra("userLat");
            userLong = intent.getStringExtra("userLong");
        }

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        UserID = user.get(SessionManager.KEY_USERID);

        tv_drivername.setText(driverName);
        tv_carNo.setText(driverCar_no);
        tv_carModel.setText(driverCar_model);
        tv_time.setText(driverTime);
        tv_timeMinute.setVisibility(View.INVISIBLE);
        tv_rating.setText(driverRating);
        Picasso.with(this)
                .load(driverImage)
                .into(driver_image);


        tv_done.setOnClickListener(this);
        rl_callDriver.setOnClickListener(this);
        rl_endTrip.setOnClickListener(this);
    }

    private void initializeMap() {

        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.myride_detail_track_your_ride_mapview)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(MyRideDetailTrackRide.this, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            }
        }

        // Changing map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Showing / hiding your current location
        googleMap.setMyLocationEnabled(false);
        // Enable / Disable zooming controls
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        // Enable / Disable my location button
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        // Enable / Disable Compass icon
        googleMap.getUiSettings().setCompassEnabled(false);
        // Enable / Disable Rotate gesture
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        // Enable / Disable zooming functionality
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.setMyLocationEnabled(false);

        if (gps.canGetLocation() && gps.isgpsenabled()) {
            double Dlatitude = gps.getLatitude();
            double Dlongitude = gps.getLongitude();

            MyCurrent_lat = Dlatitude;
            MyCurrent_long = Dlongitude;

            // Move the camera to last position with a zoom level
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Dlatitude, Dlongitude)).zoom(15).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else {
            Alert(getResources().getString(R.string.action_error), getResources().getString(R.string.alert_gpsEnable));
        }

        //set marker for driver location.
        if (driverLat != null && driverLong != null) {
            fromPosition = new LatLng(Double.parseDouble(userLat), Double.parseDouble(userLong));
            toPosition = new LatLng(Double.parseDouble(driverLat), Double.parseDouble(driverLong));

            if (fromPosition != null && toPosition != null) {
                GetRouteTask draw_route_asyncTask = new GetRouteTask();
                draw_route_asyncTask.execute();
            }
        }


    }

    @Override
    public void onClick(View v) {
        if (v == tv_done) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else if (v == rl_callDriver) {
            if (driverMobile != null) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + driverMobile));
                startActivity(callIntent);
            } else {
                Alert(MyRideDetailTrackRide.this.getResources().getString(R.string.alert_label_title), MyRideDetailTrackRide.this.getResources().getString(R.string.track_your_ride_alert_content1));
            }
        } else if (v == rl_endTrip) {
        }
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {

        final PkDialog mDialog = new PkDialog(MyRideDetailTrackRide.this);
        mDialog.setDialogTitle(title);
        mDialog.setDialogMessage(alert);
        mDialog.setPositiveButton(getResources().getString(R.string.action_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
       
    }


    //---------------AsyncTask to Draw PolyLine Between Two Point--------------
    private class GetRouteTask extends AsyncTask<String, Void, String> {

        String response = "";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... urls) {
            //Get All Route values
            document = v2GetRouteDirection.getDocument(fromPosition, toPosition, GMapV2GetRouteDirection.MODE_DRIVING);
            response = "Success";
            return response;

        }

        @Override
        protected void onPostExecute(String result) {

            if (result.equalsIgnoreCase("Success")) {
                googleMap.clear();
                ArrayList<LatLng> directionPoint = v2GetRouteDirection.getDirection(document);
                PolylineOptions rectLine = new PolylineOptions().width(10).color(
                        Color.RED);

                for (int i = 0; i < directionPoint.size(); i++) {
                    rectLine.add(directionPoint.get(i));
                }
                // Adding route on the map
                googleMap.addPolyline(rectLine);
                markerOptions.position(toPosition);
                markerOptions.position(fromPosition);
                markerOptions.draggable(true);
                //googleMap.addMarker(markerOptions);


                googleMap.addMarker(new MarkerOptions()
                        .position(toPosition)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.man_street_view)));
                googleMap.addMarker(new MarkerOptions()
                        .position(fromPosition)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_map_icon)));

                //Show path in
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(toPosition);
                builder.include(fromPosition);
                LatLngBounds bounds = builder.build();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 21));
            }
        }
    }


    //-----------------Move Back on pressed phone back button------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
            return true;
        }
        return false;
    }
}

