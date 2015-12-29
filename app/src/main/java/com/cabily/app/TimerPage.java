package com.cabily.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.cabily.iconstant.Iconstant;
import com.cabily.subclass.ActivitySubClass;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.mylibrary.dialog.PkDialog;
import com.mylibrary.volley.ServiceRequest;
import com.mylibrary.xmpp.ChatService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;

/**
 * Created by PremKumar on 9/23/2015.
 */
public class TimerPage extends ActivitySubClass {
    CircleProgressView mCircleView;
    //private CountDownTimer timer;
    int seconds = 0;
    private String retry = "";
    private String rideID = "";
    private String userID = "";

    BroadcastReceiver updateReciver;
    private SessionManager sessionManager;

    private ImageView Iv_cancelRide;
    private LinearLayout Ll_cancelRide_message;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private ServiceRequest mRequest;
    private Toast toast;
    private PkDialog mdialog;
    Handler mHandler;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timerpage);
        initialize();

        //Start XMPP Chat Service
        ChatService.startUserAction(TimerPage.this);

        // Receiving the data from broadcast
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.app.pushnotification.RideAccept");
        updateReciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                System.out.println("----------message--------------" + intent.getStringExtra("message"));

                if (intent.getStringExtra("Action").equalsIgnoreCase("ride_confirmed")) {
                    Intent i = new Intent(TimerPage.this, TrackYourRide.class);
                    i.putExtra("driverID", intent.getStringExtra("driverID"));
                    i.putExtra("driverName", intent.getStringExtra("driverName"));
                    i.putExtra("driverImage", intent.getStringExtra("driverImage"));
                    i.putExtra("driverRating", intent.getStringExtra("driverRating"));
                    i.putExtra("driverLat", intent.getStringExtra("driverLat"));
                    i.putExtra("driverLong", intent.getStringExtra("driverLong"));
                    i.putExtra("driverTime", intent.getStringExtra("driverTime"));
                    i.putExtra("rideID", intent.getStringExtra("rideID"));
                    i.putExtra("driverMobile", intent.getStringExtra("driverMobile"));
                    i.putExtra("driverCar_no", intent.getStringExtra("driverCar_no"));
                    i.putExtra("driverCar_model", intent.getStringExtra("driverCar_model"));
                    i.putExtra("userLat", intent.getStringExtra("userLatitude"));
                    i.putExtra("userLong", intent.getStringExtra("userLongitude"));
                    startActivity(i);
                    mHandler.removeCallbacks(mRunnable);
                    //timer.cancel();

                    if (mRequest != null) {
                        mRequest.cancelRequest();
                    }

                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }

            }
        };
        registerReceiver(updateReciver, filter);


        Iv_cancelRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mdialog = new PkDialog(TimerPage.this);
                mdialog.setDialogTitle("");
                mdialog.setDialogMessage(getResources().getString(R.string.timer_label_alert_cancel_ride_message));
                mdialog.setPositiveButton(getResources().getString(R.string.timer_label_alert_yes), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mdialog.dismiss();
                        cd = new ConnectionDetector(TimerPage.this);
                        isInternetPresent = cd.isConnectingToInternet();

                        if (isInternetPresent) {
                            toast.cancel();
                            DeleteRideRequest(Iconstant.delete_ride_url);
                        } else {
                            toast.setText(getResources().getString(R.string.alert_nointernet_message));
                            toast.show();
                        }
                    }
                });
                mdialog.setNegativeButton(getResources().getString(R.string.timer_label_alert_no), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mdialog.dismiss();
                    }
                });
                mdialog.show();
            }
        });

    }

    private void initialize() {
        toast = new Toast(TimerPage.this);
        sessionManager = new SessionManager(TimerPage.this);

        Iv_cancelRide = (ImageView) findViewById(R.id.timer_cancel_ride_image);
        Ll_cancelRide_message = (LinearLayout) findViewById(R.id.timer_cancel_ride_layout);
        mCircleView = (CircleProgressView) findViewById(R.id.timer_circleView);
        mCircleView.setEnabled(false);
        mCircleView.setFocusable(false);

        HashMap<String, String> userDetail = sessionManager.getUserDetails();
        userID = userDetail.get(SessionManager.KEY_USERID);

        Intent intent = getIntent();
        seconds = Integer.parseInt(intent.getStringExtra("Time")) + 1;
        retry = intent.getStringExtra("retry_count");
        rideID = intent.getStringExtra("ride_ID");

        if (retry != null && retry.length() > 0) {
            retry = "2";
        } else {
            retry = "1";
        }


        //value setting
        mCircleView.setMaxValue(seconds);
        mCircleView.setValueAnimated(0);

        //show unit
        // mCircleView.setUnit("");
        // mCircleView.setShowUnit(true);

        //text sizes
        mCircleView.setTextSize(50);
        // mCircleView.setUnitSize(40); // if i set the text size i also have to set the unit size

        // enable auto text size, previous values are overwritten
        mCircleView.setAutoTextSize(true);

        //if you want the calculated text sizes to be bigger/smaller you can do so via
        //mCircleView.setUnitScale(0.9f);
        mCircleView.setTextScale(0.6f);

        //colors of text and unit can be set via
        mCircleView.setTextColor(getResources().getColor(R.color.ripple_blue_color));


        /*timer =new CountDownTimer((seconds*1000), 500)
        {
            public void onTick(long millisUntilFinished)
            {
                long sec = millisUntilFinished/1000;

                mCircleView.setText(String.valueOf(sec));
                mCircleView.setTextMode(TextMode.TEXT);
                mCircleView.setValueAnimated(sec, 500);
            }
            public void onFinish()
            {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("Accepted_or_Not", "not");
                returnIntent.putExtra("Retry_Count", retry);
                setResult(RESULT_OK, returnIntent);
                onBackPressed();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        };
        timer.start();*/

        System.out.println("Seconds " + seconds);

        mHandler = new Handler();
        mHandler.post(mRunnable);
    }


    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (count < seconds) {
                count++;
                mCircleView.setText(String.valueOf(Math.abs(seconds - count)));
                mCircleView.setTextMode(TextMode.TEXT);
                mCircleView.setValueAnimated(count, 500);
                mHandler.postDelayed(this, 1000);
            } else {
                mHandler.removeCallbacks(this);

                if (mRequest != null) {
                    mRequest.cancelRequest();
                }

                Intent returnIntent = new Intent();
                returnIntent.putExtra("Accepted_or_Not", "not");
                returnIntent.putExtra("Retry_Count", retry);
                setResult(RESULT_OK, returnIntent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            }
        }
    };


    //-------------------Delete Ride Post Request----------------

    private void DeleteRideRequest(String Url) {

        Iv_cancelRide.setVisibility(View.GONE);
        Ll_cancelRide_message.setVisibility(View.VISIBLE);

        System.out.println("--------------Timer Delete Ride url-------------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", userID);
        jsonParams.put("ride_id", rideID);

        mRequest = new ServiceRequest(TimerPage.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("--------------Timer Delete Ride reponse-------------------" + response);

                try {
                    JSONObject object = new JSONObject(response);
                    if (object.length() > 0) {
                        String status = object.getString("status");
                        String response_value = object.getString("response");
                        if (status.equalsIgnoreCase("1")) {

                            Iv_cancelRide.setVisibility(View.VISIBLE);
                            Ll_cancelRide_message.setVisibility(View.GONE);
                            mHandler.removeCallbacks(mRunnable);


                            final PkDialog mDialog = new PkDialog(TimerPage.this);
                            mDialog.setDialogTitle(getResources().getString(R.string.action_success));
                            mDialog.setDialogMessage(response_value);
                            mDialog.setPositiveButton(getResources().getString(R.string.action_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mDialog.dismiss();

                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("Accepted_or_Not", "Cancelled");
                                    returnIntent.putExtra("Retry_Count", retry);
                                    setResult(RESULT_OK, returnIntent);
                                    finish();
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                }
                            });
                            mDialog.show();

                        } else {
                            toast.setText(response_value);
                            toast.show();
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Iv_cancelRide.setVisibility(View.VISIBLE);
                Ll_cancelRide_message.setVisibility(View.GONE);
            }

            @Override
            public void onErrorListener() {
                Iv_cancelRide.setVisibility(View.VISIBLE);
                Ll_cancelRide_message.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onDestroy() {
        //timer.cancel();

        if (mdialog != null) {
            mdialog.dismiss();
        }

        mHandler.removeCallbacks(mRunnable);
        unregisterReceiver(updateReciver);
        super.onDestroy();
    }

    //-----------------Move Back on pressed phone back button------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {
            //Do nothing
            return true;
        }
        return false;
    }

}
