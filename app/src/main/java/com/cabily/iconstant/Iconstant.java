package com.cabily.iconstant;

public interface Iconstant
{
    /*Local Url*/

        /*String BaseUrl = "http://192.168.1.251:8081/suresh/dectarfortaxi/api/v1/";
        String XMPP_HOST_URL = "192.168.1.148";
        String XMPP_SERVICE_NAME = "casp83";*/


    /*Live Url*/
        String BaseUrl="http://project.dectar.com/fortaxi/api/v1/";
        String XMPP_HOST_URL = "67.219.149.186";
        String XMPP_SERVICE_NAME = "messaging.dectar.com";



        String loginurl = BaseUrl + "app/login";
        String register_url = BaseUrl + "app/check-user";
        String register_otp_url = BaseUrl + "app/register";
        String forgot_password_url = BaseUrl + "app/user/reset-password";
        String reset_password_url = BaseUrl + "app/user/update-reset-password";
        String BookMyRide_url = BaseUrl + "app/get-map-view";
        String couponCode_apply_url = BaseUrl + "app/apply-coupon";
        String confirm_ride_url = BaseUrl + "app/book-ride";
        String delete_ride_url = BaseUrl + "app/delete-ride";
        String estimate_price_url = BaseUrl + "app/get-eta";
        String place_search_url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?types=geocode&key=AIzaSyAKDx43QL5xXBitDdviXavpqLPsGZ3uY6o&input=";
        String GetAddressFrom_LatLong_url = "https://maps.googleapis.com/maps/api/place/details/json?key=AIzaSyAKDx43QL5xXBitDdviXavpqLPsGZ3uY6o&placeid=";
        String changePassword_url= BaseUrl+"app/user/change-password";
        String profile_edit_userName_url= BaseUrl+"app/user/change-name";
        String profile_edit_mobileNo_url= BaseUrl+"app/user/change-mobile";
        String logout_url= BaseUrl+"app/logout";

        String emergencycontact_add_url= BaseUrl+"app/user/set-emergency-contact";
        String emergencycontact_view_url= BaseUrl+"app/user/view-emergency-contact";
        String emergencycontact_delete_url= BaseUrl+"app/user/delete-emergency-contact";

        String invite_earn_friends_url= BaseUrl+"app/get-invites";

        String ratecard_select_city_url= BaseUrl+"app/get-location";
        String ratecard_select_cartype_url= BaseUrl+"app/get-category";
        String ratecard_display_url= BaseUrl+"app/get-ratecard";

        String cabily_money_url= BaseUrl+"app/get-money-page";
        String cabily_add_money_url= BaseUrl+"mobile/wallet-recharge/stripe-process?";
        String cabily_money_webview_url= BaseUrl+"mobile/wallet-recharge/payform?user_id=";
        String cabily_money_transaction_url= BaseUrl+"app/get-trans-list";

        String myrides_url= BaseUrl+"app/my-rides";
        String myride_details_inVoiceEmail_url= BaseUrl+"app/mail-invoice";
        String myride_details_url= BaseUrl+"app/view-ride";
        String myride_details_track_your_ride_url= BaseUrl+"app/track-driver";
        String cancel_myride_reason_url= BaseUrl+"app/cancellation-reason";
        String cancel_myride_url= BaseUrl+"app/cancel-ride";
        String paymentList_url= BaseUrl+"app/payment-list";

        String makepayment_cash_url= BaseUrl+"app/payment/by-cash";
        String makepayment_wallet_url= BaseUrl+"app/payment/by-wallet";
        String makepayment_autoDetect_url= BaseUrl+"app/payment/by-auto-detect";
        String makepayment_Get_webview_mobileId_url= BaseUrl+"app/payment/by-gateway";
        String makepayment_webview_url= BaseUrl+"mobile/proceed-payment?mobileId=";

        String myride_rating_url= BaseUrl+"app/review/options-list";
        String myride_rating_submit_url= BaseUrl+"app/review/submit";

        String favoritelist_display_url= BaseUrl+"app/favourite/display";
        String favoritelist_add_url= BaseUrl+"app/favourite/add";
        String favoritelist_edit_url= BaseUrl+"app/favourite/edit";
        String favoritelist_delete_url= BaseUrl+"app/favourite/remove";




    //----------------------UserAgent---------------------
       String cabily_userAgent="cabily2k15android";





    //-----------------PushNotification Key--------------------

        String PushNotification_AcceptRide_Key = "ride_confirmed";
        String PushNotification_CabArrived_Key = "cab_arrived";
        String PushNotification_RideCancelled_Key = "ride_cancelled";
        String PushNotification_RideCompleted_Key = "ride_completed";
        String PushNotification_RequestPayment_Key = "requesting_payment";
        String PushNotification_PaymentPaid_Key = "payment_paid";


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


        /*Payment Paid Key*/
        String Push_Message_Payment_paid = "message";
        String Push_Action_Payment_paid = "action";
        String RideID_Payment_paid = "key1";
        String UserID_Payment_paid = "key2";

}
