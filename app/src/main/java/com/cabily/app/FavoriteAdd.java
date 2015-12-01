package com.cabily.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cabily.HockeyApp.ActivityHockeyApp;
import com.cabily.iconstant.Iconstant;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mylibrary.gps.GPSTracker;
import com.mylibrary.volley.AppController;
import com.mylibrary.volley.VolleyErrorResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Prem Kumar and Anitha on 11/13/2015.
 */
public class FavoriteAdd extends ActivityHockeyApp
{
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private SessionManager session;
    private String UserID = "";
    private String SselectedAddress="",Slatitude="",Slongitude="",Stitle="",SlocationKey="",SidentityKey="";

    private RelativeLayout Rl_back, Rl_save;
    private EditText Et_name;
    private TextView Tv_address;
    private ImageView currentLocation_image;
    private StringRequest postrequest,editRequest;
    private GoogleMap googleMap;
    GPSTracker gps;
    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    Dialog dialog;

    private RelativeLayout Rl_alert;
    private TextView Tv_alert;
    private boolean isAddressAvailable=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favourite_add);
        initialize();
        initializeMap();

        Rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // close keyboard
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(Rl_back.getWindowToken(), 0);

                onBackPressed();
                overridePendingTransition(R.anim.enter, R.anim.exit);
                finish();
            }
        });

        currentLocation_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(FavoriteAdd.this);
                isInternetPresent = cd.isConnectingToInternet();
                gps = new GPSTracker(FavoriteAdd.this);

                if (gps.isgpsenabled() && gps.canGetLocation()) {

                    double Dlatitude = gps.getLatitude();
                    double Dlongitude = gps.getLongitude();

                    // Move the camera to last position with a zoom level
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Dlatitude, Dlongitude)).zoom(17).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                } else {
                    Toast.makeText(FavoriteAdd.this, "GPS not Enabled !!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        Rl_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(FavoriteAdd.this);
                isInternetPresent = cd.isConnectingToInternet();

                if (isInternetPresent)
                {
                    if(Et_name.getText().toString().length()==0)
                    {
                        Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.favorite_add_label_name_empty));
                    }
                    else
                    {
                        if(Tv_address.getText().length()>0 && !Tv_address.getText().toString().equalsIgnoreCase(getResources().getString(R.string.favorite_add_label_gettingAddress)))
                        {
                            if(SidentityKey.equalsIgnoreCase("Edit"))
                            {
                                postRequest_FavoriteEdit(Iconstant.favoritelist_edit_url);
                            }
                            else
                            {
                                postRequest_FavoriteSave(Iconstant.favoritelist_add_url);
                            }
                        }
                        else
                        {
                            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.favorite_add_label_invalid_address));
                        }
                    }
                }
                else
                {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                }

            }
        });

        GoogleMap.OnCameraChangeListener mOnCameraChangeListener = new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                double latitude = cameraPosition.target.latitude;
                double longitude = cameraPosition.target.longitude;

                cd = new ConnectionDetector(FavoriteAdd.this);
                isInternetPresent = cd.isConnectingToInternet();

                Log.e("latitude--on_camera_change---->", "" + latitude);
                Log.e("longitude--on_camera_change---->", "" + longitude);

                if (latitude != 0.0) {
                    googleMap.clear();

                    Slatitude = String.valueOf(latitude);
                    Slongitude = String.valueOf(longitude);

                    if (isInternetPresent) {

                        if(Slatitude!=null&&Slongitude!=null)
                        {
                            GetCompleteAddressAsyncTask asyncTask=new GetCompleteAddressAsyncTask();
                            asyncTask.execute();
                        }
                        else
                        {
                            Rl_alert.setVisibility(View.VISIBLE);
                            Tv_alert.setText(getResources().getString(R.string.favorite_add_label_no_address));
                        }

                    } else {
                        Rl_alert.setVisibility(View.VISIBLE);
                        Tv_alert.setText(getResources().getString(R.string.alert_nointernet));
                    }
                }
            }
        };

        if (CheckPlayService()) {
            googleMap.setOnCameraChangeListener(mOnCameraChangeListener);
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    String tittle = marker.getTitle();
                    Log.e("tittle--on_camera_change---->", "" + tittle);
                    return true;
                }
            });
        } else {
            Alert(getResources().getString(R.string.alert_label_title), "Install Google Play service To View Location !!!");
        }

        Et_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(Et_name);
                }
                return false;
            }
        });
    }

    private void initialize()
    {
        session = new SessionManager(FavoriteAdd.this);
        cd = new ConnectionDetector(FavoriteAdd.this);
        isInternetPresent = cd.isConnectingToInternet();
        gps = new GPSTracker(FavoriteAdd.this);

        Rl_back = (RelativeLayout) findViewById(R.id.favorite_add_header_back_layout);
        Rl_save = (RelativeLayout) findViewById(R.id.favorite_add_header_save_layout);
        Et_name = (EditText)findViewById(R.id.favorite_add_name_edittext);
        Tv_address = (TextView)findViewById(R.id.favorite_add_address);
        Rl_alert = (RelativeLayout) findViewById(R.id.favorite_add_alert_layout);
        Tv_alert = (TextView)findViewById(R.id.favorite_add_alert_textView);
        currentLocation_image = (ImageView)findViewById(R.id.favorite_add_current_location_imageview);

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        UserID = user.get(SessionManager.KEY_USERID);

        Intent intent=getIntent();
        SselectedAddress=intent.getStringExtra("Intent_Address");
        Slatitude=intent.getStringExtra("Intent_Latitude");
        Slongitude=intent.getStringExtra("Intent_Longitude");
        SidentityKey=intent.getStringExtra("Intent_IdentityKey");
        if(SidentityKey.equalsIgnoreCase("Edit"))
        {
            Stitle=intent.getStringExtra("Intent_Title");
            SlocationKey=intent.getStringExtra("Intent_LocationKey");

            Et_name.setText(Stitle);
        }
    }

    private void initializeMap()
    {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.favorite_add_mapview)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(FavoriteAdd.this, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
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

        // Move the camera to last position with a zoom level
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Double.parseDouble(Slatitude), Double.parseDouble(Slongitude))).zoom(17).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    private void CloseKeyboard(EditText edittext) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(edittext.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(FavoriteAdd.this);
        dialog.setTitle(title)
                .setMessage(alert)
                .setPositiveButton(
                        "OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        }
                )
                .show();
    }

    //-----------Check Google Play Service--------
    private boolean CheckPlayService() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(FavoriteAdd.this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                final Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, FavoriteAdd.this, REQUEST_CODE_RECOVER_PLAY_SERVICES);
                if (dialog == null) {
                    Toast.makeText(FavoriteAdd.this, "incompatible version of Google Play Services", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    //-------------AsyncTask to get Complete Address------------
    public class GetCompleteAddressAsyncTask extends AsyncTask<Void, Void, String>
    {
        String strAdd = "";

        @Override
        protected void onPreExecute() {
            Tv_address.setText(getResources().getString(R.string.favorite_add_label_gettingAddress));
        }
        @Override
        protected String doInBackground(Void... params) {

            Geocoder geocoder = new Geocoder(FavoriteAdd.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(Double.parseDouble(Slatitude), Double.parseDouble(Slongitude), 1);
                if (addresses != null) {
                    Address returnedAddress = addresses.get(0);
                    StringBuilder strReturnedAddress = new StringBuilder("");

                    for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                    }
                    strAdd = strReturnedAddress.toString();
                    isAddressAvailable=true;
                } else {
                    Log.e("My Current loction address", "No Address returned!");
                    isAddressAvailable=false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("My Current loction address", "Canont get Address!");
                isAddressAvailable=false;
            }

            return strAdd;
        }

        @Override
        protected void onPostExecute(String address) {

            if(isAddressAvailable)
            {
                Rl_alert.setVisibility(View.GONE);
                Tv_address.setText(address);
            }
            else
            {
                Rl_alert.setVisibility(View.VISIBLE);
                Tv_alert.setText(getResources().getString(R.string.favorite_add_label_no_address));
                Tv_address.setText("");
            }
        }
    }


    //-----------------------Favourite Save Post Request-----------------
    private void postRequest_FavoriteSave(String Url) {
        dialog = new Dialog(FavoriteAdd.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_saving));


        System.out.println("-------------Favourite Save Url----------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------Favourite Save Response----------------" + response);
                        String Sstatus = "",Smessage="";

                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            Smessage = object.getString("message");

                            // close keyboard
                            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            mgr.hideSoftInputFromWindow(Et_name.getWindowToken(), 0);

                            if(Sstatus.equalsIgnoreCase("1"))
                            {
                                Intent local = new Intent();
                                local.setAction("com.favoriteList.refresh");
                                sendBroadcast(local);

                                final MaterialDialog dialog = new MaterialDialog(FavoriteAdd.this);
                                dialog.setTitle(getResources().getString(R.string.action_success))
                                        .setMessage(Smessage)
                                        .setPositiveButton(
                                                "OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();
                                                        onBackPressed();
                                                        overridePendingTransition(R.anim.enter, R.anim.exit);
                                                        finish();
                                                    }
                                                }
                                        )
                                        .show();
                            }
                            else
                            {
                                Alert(getResources().getString(R.string.alert_label_title), Smessage);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        dialog.dismiss();

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(FavoriteAdd.this, error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",Iconstant.cabily_userAgent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_id", UserID);
                jsonParams.put("title", Et_name.getText().toString());
                jsonParams.put("latitude", Slatitude);
                jsonParams.put("longitude", Slongitude);
                jsonParams.put("address", Tv_address.getText().toString());
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }


    //-----------------------Favourite List Edit Post Request-----------------
    private void postRequest_FavoriteEdit(String Url) {
        dialog = new Dialog(FavoriteAdd.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_saving));

        System.out.println("-------------Favourite Edit Url----------------" + Url);

        editRequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------Favourite Edit Response----------------" + response);

                        String Sstatus = "", Smessage = "";

                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            Smessage = object.getString("message");

                            // close keyboard
                            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            mgr.hideSoftInputFromWindow(Et_name.getWindowToken(), 0);

                            if(Sstatus.equalsIgnoreCase("1"))
                            {
                                Intent local = new Intent();
                                local.setAction("com.favoriteList.refresh");
                                sendBroadcast(local);

                                final MaterialDialog dialog = new MaterialDialog(FavoriteAdd.this);
                                dialog.setTitle(getResources().getString(R.string.action_success))
                                        .setMessage(Smessage)
                                        .setPositiveButton(
                                                "OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();
                                                        onBackPressed();
                                                        overridePendingTransition(R.anim.enter, R.anim.exit);
                                                        finish();
                                                    }
                                                }
                                        )
                                        .show();
                            }
                            else
                            {
                                Alert(getResources().getString(R.string.alert_label_title), Smessage);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(FavoriteAdd.this, error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",Iconstant.cabily_userAgent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_id", UserID);
                jsonParams.put("title", Et_name.getText().toString());
                jsonParams.put("latitude", Slatitude);
                jsonParams.put("longitude", Slongitude);
                jsonParams.put("address", Tv_address.getText().toString());
                jsonParams.put("location_key", SlocationKey);
                return jsonParams;
            }
        };
        editRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        editRequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(editRequest);
    }

    //-----------------Move Back on pressed phone back button------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {

            // close keyboard
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(Rl_back.getWindowToken(), 0);

            onBackPressed();
            finish();
            overridePendingTransition(R.anim.enter, R.anim.exit);
            return true;
        }
        return false;
    }

}
