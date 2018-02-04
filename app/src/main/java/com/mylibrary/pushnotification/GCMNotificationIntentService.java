package com.mylibrary.pushnotification;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.move.app.SplashPage;
import com.move.app.user.R;
import com.move.iconstant.Iconstant;
import com.move.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Anitha
 */

public class GCMNotificationIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;
    Context context = GCMNotificationIntentService.this;

    private String driverID = "", driverName = "", driverEmail = "", driverImage = "", driverRating = "",
            driverLat = "", driverLong = "", driverTime = "",rideID = "", driverMobile = "",
            driverCar_no = "", driverCar_model = "", message = "";
    String action="",msg1,title,banner;
    private static SessionManager session;
    public GCMNotificationIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);
        session = new SessionManager(context);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
               /* for (int i = 0; i < 3; i++) {
                    Log.d("Messsage Coming... ", "" + (i + 1) + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {

                    }
                }*/
                Log.e("Message Completed work @ ", "" + SystemClock.elapsedRealtime());

                Log.e("Received: ", "" + extras.toString());
                Log.e("Received: ", "" + extras.toString());

                if (extras != null) {

                    try {
                        action  = (String) extras.get(Iconstant.Push_Action);

                        if (extras.containsKey(Iconstant.DriverID)) {
                            driverID = extras.get(Iconstant.DriverID).toString();
                        }
                        if (extras.containsKey(Iconstant.DriverName)) {
                            driverName = extras.get(Iconstant.DriverName).toString();
                        }
                        if (extras.containsKey(Iconstant.DriverEmail)) {
                            driverEmail = extras.get(Iconstant.DriverEmail).toString();
                        }
                        if (extras.containsKey(Iconstant.DriverImage)) {
                            driverImage = extras.get(Iconstant.DriverImage).toString();
                        }
                        if (extras.containsKey(Iconstant.DriverRating)) {
                            driverRating = extras.get(Iconstant.DriverRating).toString();
                        }
                        if (extras.containsKey(Iconstant.DriverLat)) {
                            driverLat = extras.get(Iconstant.DriverLat).toString();
                        }
                        if (extras.containsKey(Iconstant.DriverLong)) {
                            driverLong = extras.get(Iconstant.DriverLong).toString();
                        }
                        if (extras.containsKey(Iconstant.DriverTime)) {
                            driverTime = extras.get(Iconstant.DriverTime).toString();
                        }
                        if (extras.containsKey(Iconstant.RideID)) {
                            rideID = extras.get(Iconstant.RideID).toString();
                        }
                        if (extras.containsKey(Iconstant.DriverMobile)) {
                            driverMobile = extras.get(Iconstant.DriverMobile).toString();
                        }
                        if (extras.containsKey(Iconstant.DriverCar_No)) {
                            driverCar_no = extras.get(Iconstant.DriverCar_No).toString();
                        }
                        if (extras.containsKey(Iconstant.DriverCar_Model)) {
                            driverCar_model = extras.get(Iconstant.DriverCar_Model).toString();
                        }
                        if (extras.containsKey(Iconstant.Push_Message)) {
                            message = extras.get(Iconstant.Push_Message).toString();
                        }
                        if (action.equalsIgnoreCase(Iconstant.pushNotification_Ads)) {
                            title = extras.get("key1").toString();
                            msg1 = extras.get("key2").toString();
                            banner = extras.get("key3").toString();

                        }


                        sendNotification(message.toString());

                        /*Intent local = new Intent();
                        local.setAction("com.app.pushnotification");
                        local.putExtra("driverID", driverID);
                        local.putExtra("driverName",driverName);
                        local.putExtra("driverEmail",driverEmail);
                        local.putExtra("driverImage",driverImage);
                        local.putExtra("driverRating",driverRating);
                        local.putExtra("driverLat",driverLat);
                        local.putExtra("driverLong",driverLong);
                        local.putExtra("driverTime",driverTime);
                        local.putExtra("rideID",rideID);
                        local.putExtra("driverMobile",driverMobile);
                        local.putExtra("driverCar_no",driverCar_no);
                        local.putExtra("driverCar_model",driverCar_model);
                        local.putExtra("message",message);
                        this.sendBroadcast(local);*/

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


    @SuppressWarnings("deprecation")
    private void sendNotification(String msg) {
        Intent notificationIntent = null;
        int id=createID();
        if (action.equalsIgnoreCase(Iconstant.pushNotification_Ads)) {

            /*session.setADS(true);
            session.setAds(title, msg1, banner);*/



            notificationIntent = new Intent(GCMNotificationIntentService.this, SplashPage.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            notificationIntent.putExtra("title", title);
            notificationIntent.putExtra("msg", msg1);
            notificationIntent.putExtra("banner", banner);
            notificationIntent.putExtra("ad", "true");
        }
        else
        {
            notificationIntent = new Intent(GCMNotificationIntentService.this, SplashPage.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            notificationIntent.putExtra("ad", "false");
        }

        PendingIntent contentIntent = PendingIntent.getActivity(GCMNotificationIntentService.this, id, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager nm = (NotificationManager) GCMNotificationIntentService.this.getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = GCMNotificationIntentService.this.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(GCMNotificationIntentService.this);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.app_logo)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.app_logo))
                .setTicker(msg)
                .setColor(0xFFE72F75)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("MOVE")
                .setLights(0xffff0000, 100, 2000)
                .setPriority(Notification.DEFAULT_SOUND)
                .setContentText(msg);

        Notification n = builder.getNotification();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int smallIconViewId = getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());

            if (smallIconViewId != 0) {
                if (n.contentView != null)
                    n.contentView.setViewVisibility(smallIconViewId, View.INVISIBLE);

                if (n.headsUpContentView != null)
                    n.headsUpContentView.setViewVisibility(smallIconViewId, View.INVISIBLE);

                if (n.bigContentView != null)
                    n.bigContentView.setViewVisibility(smallIconViewId, View.INVISIBLE);
            }
        }



        n.defaults |= Notification.DEFAULT_ALL;
        nm.notify(id , n);

    }
    public int createID(){
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
        return id;
    }
}