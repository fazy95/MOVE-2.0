package com.mylibrary.pushnotification;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cabily.app.SingUpAndSignIn;
import com.cabily.iconstant.Iconstant;
import com.casperon.app.cabily.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

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

    public GCMNotificationIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                for (int i = 0; i < 3; i++) {
                    Log.d("Messsage Coming... ", "" + (i + 1) + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {

                    }
                }
                Log.e("Message Completed work @ ", "" + SystemClock.elapsedRealtime());

                Log.e("Received: ", "" + extras.toString());
                Log.e("Received: ", "" + extras.toString());

                if (extras != null) {

                    try {
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

                        sendNotification(message.toString());

                        Intent local = new Intent();
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
                        this.sendBroadcast(local);

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
        notificationIntent = new Intent(GCMNotificationIntentService.this, SingUpAndSignIn.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        PendingIntent contentIntent = PendingIntent.getActivity(GCMNotificationIntentService.this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager nm = (NotificationManager) GCMNotificationIntentService.this.getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = GCMNotificationIntentService.this.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(GCMNotificationIntentService.this);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.app_logo)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.app_logo))
                .setTicker(msg)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("Cabily")
                .setLights(0xffff0000, 100, 2000)
                .setPriority(Notification.DEFAULT_SOUND)
                .setContentText(msg);

        Notification n = builder.getNotification();

        n.defaults |= Notification.DEFAULT_ALL;
        nm.notify(0, n);

    }

}