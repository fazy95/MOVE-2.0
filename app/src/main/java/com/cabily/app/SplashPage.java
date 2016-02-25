package com.cabily.app;

/**
 * Created by Prem Kumar on 10/1/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.cabily.iconstant.Iconstant;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.mylibrary.dialog.PkDialog;
import com.mylibrary.gps.GPSTracker;
import com.mylibrary.volley.ServiceRequest;
import com.mylibrary.xmpp.ChatService;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class SplashPage extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2000;
    SessionManager session;
    Context context;
    GPSTracker gps;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    PendingResult<LocationSettingsResult> result;
    final static int REQUEST_LOCATION = 199;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    private String userID = "", sLatitude = "", sLongitude = "";
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private ServiceRequest mRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        context = getApplicationContext();
        cd = new ConnectionDetector(SplashPage.this);
        isInternetPresent = cd.isConnectingToInternet();
        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.splash_avLoadingIndicatorView);


        /*try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        }
        catch (PackageManager.NameNotFoundException e) {

        }
        catch (NoSuchAlgorithmException e) {

        }*/

        // Session class instance
        session = new SessionManager(getApplicationContext());
        gps = new GPSTracker(getApplicationContext());

        mGoogleApiClient = new GoogleApiClient.Builder(SplashPage.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();


        //Enable WiFi Automatically
        /*WifiManager wifiManager = (WifiManager) SplashPage.this.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setLocation();
            }
        }, SPLASH_TIME_OUT);

    }

    @Override
    public void onConnected(Bundle bundle) {
        // enableGpsService();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    private void setLocation() {
        cd = new ConnectionDetector(SplashPage.this);
        isInternetPresent = cd.isConnectingToInternet();

        if (isInternetPresent) {
            if (gps.isgpsenabled() && gps.canGetLocation()) {
                if (session.isLoggedIn()) {
                    //starting XMPP service
                    ChatService.startUserAction(SplashPage.this);

                    HashMap<String, String> user = session.getUserDetails();
                    userID = user.get(SessionManager.KEY_USERID);
                    sLatitude = String.valueOf(gps.getLatitude());
                    sLongitude = String.valueOf(gps.getLongitude());

                    postRequest_SetUserLocation(Iconstant.setUserLocation);

                } else {
                    Intent i = new Intent(SplashPage.this, SingUpAndSignIn.class);
                    startActivity(i);
                    finish();
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                }

            } else {
                enableGpsService();
            }
        } else {


            final PkDialog mDialog = new PkDialog(SplashPage.this);
            mDialog.setDialogTitle(getResources().getString(R.string.alert_nointernet));
            mDialog.setDialogMessage(getResources().getString(R.string.alert_nointernet_message));
            mDialog.setPositiveButton(getResources().getString(R.string.timer_label_alert_retry), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    setLocation();
                }
            });
            mDialog.setNegativeButton(getResources().getString(R.string.timer_label_alert_cancel), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    finish();
                }
            });
            mDialog.show();

        }
    }

    //Enabling Gps Service
    private void enableGpsService() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        //...
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(SplashPage.this, REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        //...
                        break;
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        Toast.makeText(SplashPage.this, "Location enabled!", Toast.LENGTH_LONG).show();
                        if (session.isLoggedIn()) {
                            //starting XMPP service
                            ChatService.startUserAction(SplashPage.this);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    session = new SessionManager(getApplicationContext());
                                    gps = new GPSTracker(SplashPage.this);

                                    HashMap<String, String> user = session.getUserDetails();
                                    userID = user.get(SessionManager.KEY_USERID);
                                    sLatitude = String.valueOf(gps.getLatitude());
                                    sLongitude = String.valueOf(gps.getLongitude());

                                    postRequest_SetUserLocation(Iconstant.setUserLocation);

                                }
                            }, 2000);

                        } else {
                            Intent i = new Intent(SplashPage.this, SingUpAndSignIn.class);
                            startActivity(i);
                            finish();
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                        }

                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        finish();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
    }


    //-----------------------User Current Location Post Request-----------------
    private void postRequest_SetUserLocation(String Url) {

        System.out.println("----------sLatitude----------" + sLatitude);
        System.out.println("----------sLongitude----------" + sLongitude);
        System.out.println("----------userID----------" + userID);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", userID);
        jsonParams.put("latitude", sLatitude);
        jsonParams.put("longitude", sLongitude);

        System.out.println("-------------Splash UserLocation Url----------------" + Url);
        mRequest = new ServiceRequest(SplashPage.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("-------------Splash UserLocation Response----------------" + response);

                String Str_status = "", sCategoryID = "";
                try {
                    JSONObject object = new JSONObject(response);
                    Str_status = object.getString("status");
                    sCategoryID = object.getString("category_id");

                    if (Str_status.equalsIgnoreCase("1")) {
                        session.setCategoryID(sCategoryID);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent i = new Intent(SplashPage.this, NavigationDrawer.class);
                startActivity(i);
                finish();
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }

            @Override
            public void onErrorListener() {
                Intent i = new Intent(SplashPage.this, NavigationDrawer.class);
                startActivity(i);
                finish();
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
    }

}

