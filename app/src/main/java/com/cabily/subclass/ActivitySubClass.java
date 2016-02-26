package com.cabily.subclass;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;

import com.cabily.HockeyApp.ActivityHockeyApp;

/**
 */
public class ActivitySubClass extends ActivityHockeyApp {
    BroadcastReceiver receiver;
    protected PowerManager.WakeLock mWakeLock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.pushnotification.finish.trackyourRide");
        filter.addAction("com.pushnotification.finish.PushNotificationAlert");
        filter.addAction("com.pushnotification.finish.TimerPage");
        filter.addAction("com.pushnotification.finish.FareBreakUp");
        filter.addAction("com.pushnotification.finish.FareBreakUpPaymentList");
        filter.addAction("com.pushnotification.finish.MyRidePaymentList");
        filter.addAction("com.pushnotification.finish.MyRideDetails");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.pushnotification.finish.trackyourRide")) {
                    finish();
                } else if (intent.getAction().equals("com.pushnotification.finish.PushNotificationAlert")) {
                    finish();
                } else if (intent.getAction().equals("com.pushnotification.finish.TimerPage")) {
                    finish();
                } else if (intent.getAction().equals("com.pushnotification.finish.FareBreakUp")) {
                    finish();
                } else if (intent.getAction().equals("com.pushnotification.finish.FareBreakUpPaymentList")) {
                    finish();
                } else if (intent.getAction().equals("com.pushnotification.finish.MyRidePaymentList")) {
                    finish();
                } else if (intent.getAction().equals("com.pushnotification.finish.MyRideDetails")) {
                    finish();
                }
            }
        };
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        this.mWakeLock.release();
        super.onDestroy();
    }

}
