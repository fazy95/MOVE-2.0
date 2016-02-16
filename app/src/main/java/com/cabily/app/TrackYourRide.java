package com.cabily.app;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.cabily.iconstant.Iconstant;
import com.cabily.pojo.CancelTripPojo;
import com.cabily.subclass.ActivitySubClass;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
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
import com.mylibrary.latlnginterpolation.LatLngInterpolator;
import com.mylibrary.latlnginterpolation.MarkerAnimation;
import com.mylibrary.volley.ServiceRequest;
import com.mylibrary.widgets.RoundedImageView;
import com.mylibrary.xmpp.ChatService;
import com.squareup.picasso.Picasso;
import org.jivesoftware.smack.chat.Chat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import java.util.ArrayList;
import java.util.HashMap;

/**
 */
public class TrackYourRide extends ActivitySubClass implements View.OnClickListener {
    private TextView tv_done;
    private TextView tv_drivername, tv_carModel, tv_carNo, tv_rating, tv_time, tv_timeMinute;
    private RoundedImageView driver_image;
    private RelativeLayout rl_callDriver, rl_endTrip;
    private GoogleMap googleMap;
    private MarkerOptions marker;
    private GPSTracker gps;
    private double MyCurrent_lat = 0.0, MyCurrent_long = 0.0;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private String driverID = "", driverName = "", driverImage = "", driverRating = "",
            driverLat = "", driverLong = "", driverTime = "", rideID = "", driverMobile = "",
            driverCar_no = "", driverCar_model = "", userLat = "", userLong = "";
    private boolean isReasonAvailable = false;
    private ServiceRequest mRequest;
    Dialog dialog;
    private SessionManager session;
    private String UserID = "";
    ArrayList<CancelTripPojo> itemlist_reason;
    public static TrackYourRide trackyour_ride_class;
    private TextView Tv_headerTitle;
    private View track_your_ride_view1;
    private View arriveView;
    private RelativeLayout Rl_arriveLayout;
    LatLng fromPosition;
    LatLng toPosition;
    MarkerOptions markerOptions;
    private static Marker curentDriverMarker;

    public class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.package.ACTION_CLASS_TrackYourRide_REFRESH_Arrived_Driver")) {
                Tv_headerTitle.setText("Driver Has Arrived");
                rl_endTrip.setVisibility(View.GONE);
                track_your_ride_view1.setVisibility(View.GONE);
                Rl_arriveLayout.setVisibility(View.INVISIBLE);
                arriveView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private RefreshReceiver refreshReceiver;


    static LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Spherical();
    public static void updateMap(LatLng latLng) {
        MarkerAnimation.animateMarkerToICS(curentDriverMarker, latLng, latLngInterpolator);
    }


    public static class FragmentMessage extends Handler {

        @Override
        public void handleMessage(Message message) {
            String data = message.obj.toString();
            Log.d("Hello","Message +++++++++++++++++++++++"+ data);
            try{
                String[] array = data.split(",");
                float lat =  Float.valueOf(array[1]);
                float lng = Float.valueOf(array[2]);
                float bearing = Float.valueOf(array[3]);
                if(curentDriverMarker != null){
                    LatLng mLatLng = new LatLng(lat,lng);
                    curentDriverMarker.setPosition(mLatLng);
                    updateMap(mLatLng);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void createChat(){
        ChatService.startUserAction(getApplicationContext());
        String sSenderID=""+driverID;
        String  sToID = sSenderID + "@" + Iconstant.XMPP_SERVICE_NAME;
        Chat chat = ChatService.createChat(sToID);
        ChatService.setChatMessenger(new Messenger(new FragmentMessage()));
        ChatService.enableChat();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_your_ride);
        trackyour_ride_class = TrackYourRide.this;
        initialize();
        initializeMap();
        //Start XMPP Chat Service
        createChat();
    }



    private void initialize() {
        cd = new ConnectionDetector(TrackYourRide.this);
        isInternetPresent = cd.isConnectingToInternet();
        gps = new GPSTracker(TrackYourRide.this);
        session = new SessionManager(TrackYourRide.this);
        itemlist_reason = new ArrayList<CancelTripPojo>();
        markerOptions = new MarkerOptions();

        tv_done = (TextView) findViewById(R.id.track_your_ride_done_textview);
        tv_drivername = (TextView) findViewById(R.id.track_your_ride_driver_name);
        tv_carModel = (TextView) findViewById(R.id.track_your_ride_driver_carmodel);
        tv_carNo = (TextView) findViewById(R.id.track_your_ride_driver_carNo);
        tv_rating = (TextView) findViewById(R.id.track_your_ride_driver_rating);
        tv_time = (TextView) findViewById(R.id.track_your_ride_arrive_time_textview);
        tv_timeMinute = (TextView) findViewById(R.id.track_your_ride_arrive_minute_textview);
        driver_image = (RoundedImageView) findViewById(R.id.track_your_ride_driverimage);
        rl_callDriver = (RelativeLayout) findViewById(R.id.track_your_ride_calldriver_layout);
        rl_endTrip = (RelativeLayout) findViewById(R.id.track_your_ride_endtrip_layout);
        Rl_arriveLayout = (RelativeLayout) findViewById(R.id.track_your_ride_label_arrival_layout);
        arriveView = (View) findViewById(R.id.track_your_ride_drive_info_view);

        Tv_headerTitle = (TextView) findViewById(R.id.track_your_ride_track_label);
        track_your_ride_view1 = (View) findViewById(R.id.track_your_ride_view1);


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


        // -----code to refresh drawer using broadcast receiver-----
        refreshReceiver = new RefreshReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.package.ACTION_CLASS_TrackYourRide_REFRESH_Arrived_Driver");
        registerReceiver(refreshReceiver, intentFilter);

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
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.track_your_ride_mapview)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(TrackYourRide.this, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
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
            Intent finish_timerPage = new Intent();
            finish_timerPage.setAction("com.pushnotification.finish.TimerPage");
            sendBroadcast(finish_timerPage);
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("com.pushnotification.updateBottom_view");
            sendBroadcast(broadcastIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else if (v == rl_callDriver) {
            if (driverMobile != null) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + driverMobile));
                startActivity(callIntent);
            } else {
                Alert(TrackYourRide.this.getResources().getString(R.string.alert_label_title), TrackYourRide.this.getResources().getString(R.string.track_your_ride_alert_content1));
            }
        } else if (v == rl_endTrip) {


            final PkDialog mDialog = new PkDialog(TrackYourRide.this);
            mDialog.setDialogTitle(getResources().getString(R.string.my_rides_detail_cancel_ride_alert_title));
            mDialog.setDialogMessage(getResources().getString(R.string.my_rides_detail_cancel_ride_alert));
            mDialog.setPositiveButton(getResources().getString(R.string.my_rides_detail_cancel_ride_alert_yes), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();

                    cd = new ConnectionDetector(TrackYourRide.this);
                    isInternetPresent = cd.isConnectingToInternet();

                    if (isInternetPresent) {
                        postRequest_CancelRides_Reason(Iconstant.cancel_myride_reason_url);
                    } else {
                        Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                    }
                }
            });
            mDialog.setNegativeButton(getResources().getString(R.string.my_rides_detail_cancel_ride_alert_no), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
            mDialog.show();

        }
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {

        final PkDialog mDialog = new PkDialog(TrackYourRide.this);
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
        GMapV2GetRouteDirection v2GetRouteDirection = new GMapV2GetRouteDirection();
        Document document;

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
                        Color.BLUE);
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
                curentDriverMarker =  googleMap.addMarker(new MarkerOptions()
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


    //-----------------------MyRide Cancel Reason Post Request-----------------
    private void postRequest_CancelRides_Reason(String Url) {
        dialog = new Dialog(TrackYourRide.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_pleasewait));


        System.out.println("-------------MyRide Cancel Reason Url----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);

        mRequest = new ServiceRequest(TrackYourRide.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("-------------MyRide Cancel Reason Response----------------" + response);

                String Sstatus = "";

                try {
                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    if (Sstatus.equalsIgnoreCase("1")) {
                        JSONObject response_object = object.getJSONObject("response");
                        if (response_object.length() > 0) {
                            JSONArray reason_array = response_object.getJSONArray("reason");
                            if (reason_array.length() > 0) {
                                itemlist_reason.clear();
                                for (int i = 0; i < reason_array.length(); i++) {
                                    JSONObject reason_object = reason_array.getJSONObject(i);
                                    CancelTripPojo pojo = new CancelTripPojo();
                                    pojo.setReason(reason_object.getString("reason"));
                                    pojo.setReasonId(reason_object.getString("id"));

                                    itemlist_reason.add(pojo);
                                }

                                isReasonAvailable = true;
                            } else {
                                isReasonAvailable = false;
                            }
                        }
                    } else {
                        String Sresponse = object.getString("response");
                        Alert(getResources().getString(R.string.alert_label_title), Sresponse);
                    }


                    if (Sstatus.equalsIgnoreCase("1") && isReasonAvailable) {
                        Intent finish_timerPage = new Intent();
                        finish_timerPage.setAction("com.pushnotification.finish.TimerPage");
                        sendBroadcast(finish_timerPage);

                        Intent passIntent = new Intent(TrackYourRide.this, TrackRideCancelTrip.class);
                        Bundle bundleObject = new Bundle();
                        bundleObject.putSerializable("Reason", itemlist_reason);
                        passIntent.putExtras(bundleObject);
                        passIntent.putExtra("RideID", rideID);
                        startActivity(passIntent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                dialog.dismiss();
            }

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });
    }

    //-----------------Move Back on pressed phone back button------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {

            Intent finish_timerPage = new Intent();
            finish_timerPage.setAction("com.pushnotification.finish.TimerPage");
            sendBroadcast(finish_timerPage);

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("com.pushnotification.updateBottom_view");
            sendBroadcast(broadcastIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();

            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        // Unregister the logout receiver
        unregisterReceiver(refreshReceiver);
        ChatService.disableChat();
        super.onDestroy();
    }
}
