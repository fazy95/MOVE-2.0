package com.cabily.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.KeyEvent;

import com.cabily.subclass.ActivitySubClass;
import com.casperon.app.cabily.R;
import com.mylibrary.xmpp.ChatService;


import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;

/**
 * Created by PremKumar on 9/23/2015.
 */
public class TimerPage extends ActivitySubClass
{
    CircleProgressView mCircleView;
    //private CountDownTimer timer;
    int seconds=0;
    private String retry="";

    BroadcastReceiver updateReciver;

    Handler mHandler;
    int count=0;
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

                System.out.println("----------message--------------"+intent.getStringExtra("message"));

                if(intent.getStringExtra("Action").equalsIgnoreCase("ride_confirmed"))
                {
                    Intent i=new Intent(TimerPage.this,TrackYourRide.class);
                    i.putExtra("driverID", intent.getStringExtra("driverID"));
                    i.putExtra("driverName",intent.getStringExtra("driverName"));
                    i.putExtra("driverImage",intent.getStringExtra("driverImage"));
                    i.putExtra("driverRating",intent.getStringExtra("driverRating"));
                    i.putExtra("driverLat",intent.getStringExtra("driverLat"));
                    i.putExtra("driverLong",intent.getStringExtra("driverLong"));
                    i.putExtra("driverTime",intent.getStringExtra("driverTime"));
                    i.putExtra("rideID",intent.getStringExtra("rideID"));
                    i.putExtra("driverMobile",intent.getStringExtra("driverMobile"));
                    i.putExtra("driverCar_no",intent.getStringExtra("driverCar_no"));
                    i.putExtra("driverCar_model",intent.getStringExtra("driverCar_model"));
                    i.putExtra("userLat",intent.getStringExtra("userLatitude"));
                    i.putExtra("userLong", intent.getStringExtra("userLongitude"));
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    mHandler.removeCallbacks(mRunnable);
                    //timer.cancel();
                    finish();
                }

            }
        };
        registerReceiver(updateReciver, filter);
    }

    private void initialize()
    {
        mCircleView = (CircleProgressView) findViewById(R.id.timer_circleView);
        mCircleView.setEnabled(false);
        mCircleView.setFocusable(false);

        Intent intent =getIntent();
        seconds=Integer.parseInt(intent.getStringExtra("Time"))+1;
        retry=intent.getStringExtra("retry_count");

        if(retry!=null && retry.length()>0)
        {
            retry="2";
        }
        else
        {
            retry="1";
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

        System.out.println("Seconds "+ seconds);

        mHandler = new Handler();
        mHandler.post(mRunnable);
    }


    Runnable mRunnable=new Runnable() {
        @Override
        public void run() {
            if(count < seconds){
                count++;
                mCircleView.setText(String.valueOf(Math.abs(seconds - count)));
                mCircleView.setTextMode(TextMode.TEXT);
                mCircleView.setValueAnimated(count, 500);
                mHandler.postDelayed(this,1000);
            }else{
                mHandler.removeCallbacks(this);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("Accepted_or_Not", "not");
                returnIntent.putExtra("Retry_Count", retry);
                setResult(RESULT_OK, returnIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }
    };

    @Override
    public void onDestroy() {
        //timer.cancel();
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
