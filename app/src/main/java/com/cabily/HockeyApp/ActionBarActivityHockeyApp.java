package com.cabily.HockeyApp;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

/**
 * Created by Prem Kumar and Anitha on 11/12/2015.
 */
public class ActionBarActivityHockeyApp extends ActionBarActivity {
 private static String APP_ID = "9f8e1861d5cc413ba593e3367676bca3";
    PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkForUpdates();
        final PowerManager pm
                = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForCrashes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterManagers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterManagers();
        if(mWakeLock != null){
            mWakeLock.release();
        }
    }

    private void checkForCrashes() {
        CrashManager.register(this, APP_ID);
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this, APP_ID);
    }

    private void unregisterManagers() {
        UpdateManager.unregister();
        // unregister other managers if necessary...
    }

}

