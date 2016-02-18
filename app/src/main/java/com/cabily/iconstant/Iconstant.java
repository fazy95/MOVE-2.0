package com.cabily.iconstant;

public interface Iconstant
{
    /*Local Url*/

       // String BaseUrl = "http://192.168.1.251:8081/suresh/dectarfortaxi/";
      //  String XMPP_HOST_URL = "192.168.1.150";
      //  String XMPP_SERVICE_NAME = "casp83";

     /*Testing Url*/
        String BaseUrl = "http://project.dectar.com/cabilydemo/";
        String XMPP_HOST_URL = "67.219.149.186";
        String XMPP_SERVICE_NAME = "messaging.dectar.com";

    /*Live Url*/
       /* String BaseUrl="http://project.dectar.com/fortaxi/";
        String XMPP_HOST_URL = "67.219.149.186";
        String XMPP_SERVICE_NAME = "messaging.dectar.com";*/
        String setUserLocation=BaseUrl+"api/v1/app/set-user-geo";
        String loginurl = BaseUrl + "api/v1/app/login";
        String register_url = BaseUrl + "api/v1/app/check-user";
        String faceboo_register_url = BaseUrl + "api/v1/app/social-login";

        String register_otp_url = BaseUrl + "api/v1/app/register";
        String forgot_password_url = BaseUrl + "api/v1/app/user/reset-password";
        String reset_password_url = BaseUrl + "api/v1/app/user/update-reset-password";
        String BookMyRide_url = BaseUrl + "api/v1/app/get-map-view";
        String couponCode_apply_url = BaseUrl + "api/v1/app/apply-coupon";
        String confirm_ride_url = BaseUrl + "api/v1/app/book-ride";
        String delete_ride_url = BaseUrl + "api/v1/app/delete-ride";
        String estimate_price_url = BaseUrl + "api/v1/app/get-eta";
        String place_search_url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?types=geocode&key=AIzaSyAKDx43QL5xXBitDdviXavpqLPsGZ3uY6o&input=";
        String GetAddressFrom_LatLong_url = "https://maps.googleapis.com/maps/api/place/details/json?key=AIzaSyAKDx43QL5xXBitDdviXavpqLPsGZ3uY6o&placeid=";
        String changePassword_url= BaseUrl+"api/v1/app/user/change-password";
        String profile_edit_userName_url= BaseUrl+"api/v1/app/user/change-name";
        String profile_edit_mobileNo_url= BaseUrl+"api/v1/app/user/change-mobile";
        String logout_url= BaseUrl+"api/v1/app/logout";

        String emergencycontact_add_url= BaseUrl+"api/v1/app/user/set-emergency-contact";
        String emergencycontact_view_url= BaseUrl+"api/v1/app/user/view-emergency-contact";
        String emergencycontact_delete_url= BaseUrl+"api/v1/app/user/delete-emergency-contact";

        String invite_earn_friends_url= BaseUrl+"api/v1/app/get-invites";

        String ratecard_select_city_url= BaseUrl+"api/v1/app/get-location";
        String ratecard_select_cartype_url= BaseUrl+"api/v1/app/get-category";
        String ratecard_display_url= BaseUrl+"api/v1/app/get-ratecard";

        String cabily_money_url= BaseUrl+"api/v1/app/get-money-page";
        String cabily_add_money_url= BaseUrl+"api/v1/mobile/wallet-recharge/stripe-process?";
        String cabily_money_webview_url= BaseUrl+"api/v1/mobile/wallet-recharge/payform?user_id=";
        String cabily_money_transaction_url= BaseUrl+"api/v1/app/get-trans-list";

        String myrides_url= BaseUrl+"api/v1/app/my-rides";
        String myride_details_inVoiceEmail_url= BaseUrl+"api/v1/app/mail-invoice";
        String myride_details_url= BaseUrl+"api/v1/app/view-ride";
        String myride_details_track_your_ride_url= BaseUrl+"api/v3/track-driver";
        String cancel_myride_reason_url= BaseUrl+"api/v1/app/cancellation-reason";
        String cancel_myride_url= BaseUrl+"api/v1/app/cancel-ride";
        String paymentList_url= BaseUrl+"api/v1/app/payment-list";

        String makepayment_cash_url= BaseUrl+"api/v1/app/payment/by-cash";
        String makepayment_wallet_url= BaseUrl+"api/v1/app/payment/by-wallet";
        String makepayment_autoDetect_url= BaseUrl+"api/v1/app/payment/by-auto-detect";
        String makepayment_Get_webview_mobileId_url= BaseUrl+"api/v1/app/payment/by-gateway";
        String makepayment_webview_url= BaseUrl+"api/v1/mobile/proceed-payment?mobileId=";

        String myride_rating_url= BaseUrl+"api/v1/app/review/options-list";
        String myride_rating_submit_url= BaseUrl+"api/v1/app/review/submit";

        String favoritelist_display_url= BaseUrl+"api/v1/app/favourite/display";
        String favoritelist_add_url= BaseUrl+"api/v1/app/favourite/add";
        String favoritelist_edit_url= BaseUrl+"api/v1/app/favourite/edit";
        String favoritelist_delete_url= BaseUrl+"api/v1/app/favourite/remove";

        String tip_add_url= BaseUrl+"api/v1/app/apply-tips";
        String tip_remove_url= BaseUrl+"api/v1/app/remove-tips";




    //----------------------UserAgent---------------------
       String cabily_userAgent="cabily2k15android";
       String cabily_IsApplication="1";
       String cabily_AppLanguage="en";





    //-----------------PushNotification Key--------------------

        String PushNotification_AcceptRide_Key = "ride_confirmed";
        String PushNotification_CabArrived_Key = "cab_arrived";
        String PushNotification_RideCancelled_Key = "ride_cancelled";
        String PushNotification_RideCompleted_Key = "ride_completed";
        String PushNotification_RequestPayment_Key = "requesting_payment";
        String PushNotification_PaymentPaid_Key = "payment_paid";
        String pushNotificationBeginTrip ="trip_begin";


        /*Ride Accept Key*/
        String DriverID = "key1";
        String DriverName = "key2";
        String DriverEmail = "key3";
        String DriverImage = "key4";
        String DriverRating = "key5";
        String DriverLat = "key6";
        String DriverLong = "key7";
        String DriverTime = "key8";
        String RideID = "key9";
        String DriverMobile = "key10";
        String DriverCar_No = "key11";
        String DriverCar_Model = "key12";
        String UserLat = "key14";
        String UserLong = "key15";
        String Push_Message = "message";
        String Push_Action = "action";


        /*Ride Arrived Key*/
        String UserID_Arrived = "key1";
        String RideID_Arrived = "key2";
        String Push_Message_Arrived = "message";
        String Push_Action_Arrived = "action";

        /*Ride Cancelled Key*/
        String RideID_Cancelled = "key1";
        String Push_Message_Cancelled = "message";
        String Push_Action_Cancelled = "action";

        /*Ride Completed Key*/
        String Push_Message_Completed = "message";
        String Push_Action_Completed = "action";

        /*Request Payment Key*/
        String Push_Message_Request_Payment = "message";
        String Push_Action_Request_Payment = "action";
        String CurrencyCode_Request_Payment = "key1";
        String TotalAmount_Request_Payment = "key2";
        String TravelDistance_Request_Payment = "key3";
        String Duration_Request_Payment = "key4";
        String WaitingTime_Request_Payment = "key5";
        String RideID_Request_Payment = "key6";
        String UserID_Request_Payment = "key7";
        String TipStatus_Request_Payment = "key8";
        String TipAmount_Request_Payment = "key9";
        String DriverName_Request_Payment = "key10";
        String DriverImage_Request_Payment = "key11";
        String DriverRating_Request_Payment = "key12";
        String Driver_Latitude_Request_Payment = "key13";
        String Driver_Longitude_Request_Payment = "key14";
        String UserName_Request_Payment = "key15";
        String User_Latitude_Request_Payment = "key16";
        String User_Longitude_Request_Payment = "key17";
        String subTotal_Request_Payment = "key18";
        String coupon_Request_Payment = "key19";
        String serviceTax_Request_Payment = "key20";
        String Total_Request_Payment = "key21";

        /*Payment Paid Key*/
        String Push_Message_Payment_paid = "message";
        String Push_Action_Payment_paid = "action";
        String RideID_Payment_paid = "key1";
        String UserID_Payment_paid = "key2";

}
