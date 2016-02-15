package com.mylibrary.xmpp;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.cabily.app.AboutUs;
import com.cabily.app.FareBreakUp;
import com.cabily.app.PushNotificationAlert;
import com.cabily.app.SingUpAndSignIn;
import com.cabily.iconstant.Iconstant;
import com.casperon.app.cabily.R;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONObject;

import java.net.URLDecoder;

/**
 * Created by user88 on 11/4/2015.
 */
public class ChatHandler {
    private Context context;
    private IntentService service;

    public ChatHandler(Context context, IntentService service) {
        this.context = context;
        this.service = service;
    }

    public void onHandleChatMessage(Message message) {
        try {
            String data = URLDecoder.decode(message.getBody(), "UTF-8");
            JSONObject messageObject = new JSONObject(data);

            if (messageObject.length() > 0) {
                System.out.println("--------------xmpp service data----------------------" + data);
                String action = (String) messageObject.get(Iconstant.Push_Action);
                if (action.equalsIgnoreCase(Iconstant.PushNotification_AcceptRide_Key))
                {
                    sendBroadCastToRideConfirm(messageObject);
                }
                else if (action.equalsIgnoreCase(Iconstant.PushNotification_CabArrived_Key))
                {
                    showCabArrivedAlert(messageObject);
                }
                else if (action.equalsIgnoreCase(Iconstant.PushNotification_RideCancelled_Key))
                {
                    rideCancelledAlert(messageObject);
                }
                else if (action.equalsIgnoreCase(Iconstant.PushNotification_RideCompleted_Key))
                {
                    rideCompletedAlert(messageObject);
                }
                else if (action.equalsIgnoreCase(Iconstant.PushNotification_RequestPayment_Key))
                {
                    requestPayment(messageObject);
                }
                else if (action.equalsIgnoreCase(Iconstant.PushNotification_PaymentPaid_Key))
                {
                    paymentPaid(messageObject);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendBroadCastToRideConfirm(JSONObject messageObject) throws Exception {
        Intent local = new Intent();
        local.setAction("com.app.pushnotification.RideAccept");
        local.putExtra("driverID", messageObject.getString(Iconstant.DriverID));
        local.putExtra("driverName", messageObject.getString(Iconstant.DriverName));
        local.putExtra("driverEmail", messageObject.getString(Iconstant.DriverEmail));
        local.putExtra("driverImage", messageObject.getString(Iconstant.DriverImage));
        local.putExtra("driverRating", messageObject.getString(Iconstant.DriverRating));
        local.putExtra("driverLat", messageObject.getString(Iconstant.DriverLat));
        local.putExtra("driverLong", messageObject.getString(Iconstant.DriverLong));
        local.putExtra("driverTime", messageObject.getString(Iconstant.DriverTime));
        local.putExtra("rideID", messageObject.getString(Iconstant.RideID));
        local.putExtra("driverMobile", messageObject.getString(Iconstant.DriverMobile));
        local.putExtra("driverCar_no", messageObject.getString(Iconstant.DriverCar_No));
        local.putExtra("driverCar_model", messageObject.getString(Iconstant.DriverCar_Model));
        local.putExtra("userLatitude", messageObject.getString(Iconstant.UserLat));
        local.putExtra("userLongitude", messageObject.getString(Iconstant.UserLong));
        local.putExtra("message", messageObject.getString(Iconstant.Push_Message));
        local.putExtra("Action", messageObject.getString(Iconstant.Push_Action));
        context.sendBroadcast(local);
    }

    private void showCabArrivedAlert(JSONObject messageObject) throws Exception
    {
        //refreshMethod();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.package.ACTION_CLASS_TrackYourRide_REFRESH_Arrived_Driver");
        context.sendBroadcast(broadcastIntent);

       // sendNotification(messageObject.getString(Iconstant.Push_Message_Arrived));


        /*Intent i1=new Intent(context, PushNotificationAlert.class);
        i1.putExtra("message", messageObject.getString(Iconstant.Push_Message_Arrived));
        i1.putExtra("Action", messageObject.getString(Iconstant.Push_Action_Arrived));
        i1.putExtra("UserID", messageObject.getString(Iconstant.UserID_Arrived));
        i1.putExtra("RideID", messageObject.getString(Iconstant.RideID_Arrived));
        i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i1);*/
    }

    private void rideCancelledAlert(JSONObject messageObject) throws Exception
    {
        refreshMethod();

        Intent i1=new Intent(context, PushNotificationAlert.class);
        i1.putExtra("message", messageObject.getString(Iconstant.Push_Message_Cancelled));
        i1.putExtra("Action", messageObject.getString(Iconstant.Push_Action_Cancelled));
        i1.putExtra("RideID", messageObject.getString(Iconstant.RideID_Cancelled));
        i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i1);
    }


    private void rideCompletedAlert(JSONObject messageObject) throws Exception
    {
        refreshMethod();

        Intent i1=new Intent(context, PushNotificationAlert.class);
        i1.putExtra("message", context.getResources().getString(R.string.pushnotification_alert_label_ride_completed));
        i1.putExtra("Action", messageObject.getString(Iconstant.Push_Action_Completed));
        i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i1);

    }

    private void requestPayment(JSONObject messageObject) throws Exception
    {
        refreshMethod();

        Intent i1=new Intent(context, FareBreakUp.class);
        i1.putExtra("message", messageObject.getString(Iconstant.Push_Message_Request_Payment));
        i1.putExtra("Action", messageObject.getString(Iconstant.Push_Action_Request_Payment));
        i1.putExtra("CurrencyCode", messageObject.getString(Iconstant.CurrencyCode_Request_Payment));
        i1.putExtra("TotalAmount", messageObject.getString(Iconstant.TotalAmount_Request_Payment));
        i1.putExtra("TravelDistance", messageObject.getString(Iconstant.TravelDistance_Request_Payment));
        i1.putExtra("Duration", messageObject.getString(Iconstant.Duration_Request_Payment));
        i1.putExtra("WaitingTime", messageObject.getString(Iconstant.WaitingTime_Request_Payment));
        i1.putExtra("RideID", messageObject.getString(Iconstant.RideID_Request_Payment));
        i1.putExtra("UserID", messageObject.getString(Iconstant.UserID_Request_Payment));
        i1.putExtra("DriverName", messageObject.getString(Iconstant.DriverName_Request_Payment));
        i1.putExtra("DriverImage", messageObject.getString(Iconstant.DriverImage_Request_Payment));
        i1.putExtra("DriverRating", messageObject.getString(Iconstant.DriverRating_Request_Payment));
        i1.putExtra("DriverLatitude", messageObject.getString(Iconstant.Driver_Latitude_Request_Payment));
        i1.putExtra("DriverLongitude", messageObject.getString(Iconstant.Driver_Longitude_Request_Payment));
        i1.putExtra("UserName", messageObject.getString(Iconstant.UserName_Request_Payment));
        i1.putExtra("UserLatitude", messageObject.getString(Iconstant.User_Latitude_Request_Payment));
        i1.putExtra("UserLongitude", messageObject.getString(Iconstant.User_Longitude_Request_Payment));
        i1.putExtra("SubTotal", messageObject.getString(Iconstant.subTotal_Request_Payment));
        i1.putExtra("ServiceTax", messageObject.getString(Iconstant.serviceTax_Request_Payment));
        i1.putExtra("TotalPayment", messageObject.getString(Iconstant.Total_Request_Payment));
        i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i1);
    }

    private void paymentPaid(JSONObject messageObject) throws Exception
    {
        refreshMethod();

        Intent finish_fareBreakUp = new Intent();
        finish_fareBreakUp.setAction("com.pushnotification.finish.FareBreakUpPaymentList");
        context.sendBroadcast(finish_fareBreakUp);

        Intent finish_MyRidePaymentList = new Intent();
        finish_fareBreakUp.setAction("com.pushnotification.finish.MyRidePaymentList");
        context.sendBroadcast(finish_MyRidePaymentList);

        Intent i1=new Intent(context, PushNotificationAlert.class);
        i1.putExtra("message", messageObject.getString(Iconstant.Push_Message_Payment_paid));
        i1.putExtra("Action", messageObject.getString(Iconstant.Push_Action_Payment_paid));
        i1.putExtra("RideID", messageObject.getString(Iconstant.RideID_Payment_paid));
        i1.putExtra("UserID", messageObject.getString(Iconstant.UserID_Payment_paid));
        i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i1);
    }



    private void refreshMethod()
    {
        Intent finish_fareBreakUp = new Intent();
        finish_fareBreakUp.setAction("com.pushnotification.finish.FareBreakUp");
        context.sendBroadcast(finish_fareBreakUp);

        Intent finish_timerPage = new Intent();
        finish_timerPage.setAction("com.pushnotification.finish.TimerPage");
        context.sendBroadcast(finish_timerPage);

        Intent finish_pushAlert = new Intent();
        finish_pushAlert.setAction("com.pushnotification.finish.PushNotificationAlert");
        context.sendBroadcast(finish_pushAlert);

        Intent finish_MyRideDetails = new Intent();
        finish_MyRideDetails.setAction("com.pushnotification.finish.MyRideDetails");
        context.sendBroadcast(finish_MyRideDetails);

        Intent local = new Intent();
        local.setAction("com.pushnotification.finish.trackyourRide");
        context.sendBroadcast(local);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.pushnotification.updateBottom_view");
        context.sendBroadcast(broadcastIntent);

    }

    private void trackRideRefreshMethod()
    {
        Intent finish_fareBreakUp = new Intent();
        finish_fareBreakUp.setAction("com.pushnotification.finish.FareBreakUp");
        context.sendBroadcast(finish_fareBreakUp);

        Intent finish_timerPage = new Intent();
        finish_timerPage.setAction("com.pushnotification.finish.TimerPage");
        context.sendBroadcast(finish_timerPage);

        Intent finish_pushAlert = new Intent();
        finish_pushAlert.setAction("com.pushnotification.finish.PushNotificationAlert");
        context.sendBroadcast(finish_pushAlert);

        Intent finish_MyRideDetails = new Intent();
        finish_MyRideDetails.setAction("com.pushnotification.finish.MyRideDetails");
        context.sendBroadcast(finish_MyRideDetails);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.pushnotification.updateBottom_view");
        context.sendBroadcast(broadcastIntent);

    }



    private void sendNotification(String msg) {
        Intent notificationIntent = null;
      
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = context.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
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
