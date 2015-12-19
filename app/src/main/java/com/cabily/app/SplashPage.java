package com.cabily.app;

/**
 * Created by Prem Kumar on 10/1/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

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
import com.mylibrary.gps.GPSTracker;
import com.mylibrary.xmpp.ChatService;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        context = getApplicationContext();

        // Session class instance
        session = new SessionManager(getApplicationContext());
        gps = new GPSTracker(getApplicationContext());

        mGoogleApiClient = new GoogleApiClient.Builder(SplashPage.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();


        //Enable WiFi Automatically
        WifiManager wifiManager = (WifiManager) SplashPage.this.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }


        if(gps.isgpsenabled()&&gps.canGetLocation())
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (session.isLoggedIn()) {
                        //starting XMPP service
                        ChatService.startUserAction(SplashPage.this);

                        Intent i = new Intent(SplashPage.this, NavigationDrawer.class);
                        startActivity(i);
                        finish();
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                    } else {
                        Intent i = new Intent(SplashPage.this, SingUpAndSignIn.class);
                        startActivity(i);
                        finish();
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                }
            }, SPLASH_TIME_OUT);
        }
        else
        {
            enableGpsService();
        }

    }


   /* private void EnableLocation() {
        final Dialog dialog = new Dialog(SplashPage.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.gps_dialog);

        TextView tv_no = (TextView) dialog.findViewById(R.id.gps_popup_text_no);
        TextView tv_yes = (TextView) dialog.findViewById(R.id.gps_popup_text_ok);

        tv_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                finish();
            }
        });

        tv_yes.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View arg0) {

                turnGPSOn();

                WifiManager wifiManager = (WifiManager) SplashPage.this.getSystemService(Context.WIFI_SERVICE);
                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }*/



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


    //Enabling Gps Service
    private void enableGpsService()
    {
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
                            status.startResolutionForResult(SplashPage.this,REQUEST_LOCATION);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_LOCATION:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                    {
                        Toast.makeText(SplashPage.this, "Location enabled!", Toast.LENGTH_LONG).show();
                        if (session.isLoggedIn()) {
                            //starting XMPP service
                            ChatService.startUserAction(SplashPage.this);

                            Intent i = new Intent(SplashPage.this, NavigationDrawer.class);
                            startActivity(i);
                            finish();
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                        } else {
                            Intent i = new Intent(SplashPage.this, SingUpAndSignIn.class);
                            startActivity(i);
                            finish();
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                        }

                        break;
                    }
                    case Activity.RESULT_CANCELED:
                    {
                        finish();
                        break;
                    }
                    default:
                    {
                        break;
                    }
                }
                break;
        }
    }



}

