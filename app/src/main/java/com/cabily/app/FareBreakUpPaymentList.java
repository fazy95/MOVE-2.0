package com.cabily.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cabily.HockeyApp.ActivityHockeyApp;
import com.cabily.adapter.MyRidePaymentListAdapter;
import com.cabily.iconstant.Iconstant;
import com.cabily.pojo.PaymentListPojo;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;
import com.mylibrary.volley.AppController;
import com.mylibrary.volley.VolleyErrorResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Prem Kumar and Anitha on 11/7/2015.
 */
public class FareBreakUpPaymentList extends ActivityHockeyApp {
    private RelativeLayout back;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private SessionManager session;
    private String UserID = "";

    private StringRequest postrequest;
    Dialog dialog;
    ArrayList<PaymentListPojo> itemlist;
    MyRidePaymentListAdapter adapter;
    private ExpandableHeightListView listview;
    private String SrideId_intent = "";
    private boolean isPaymentAvailable=false;
    private String SpaymentCode="";

    public static FareBreakUpPaymentList myride_paymentList_class;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myride_payment_list);
        myride_paymentList_class=FareBreakUpPaymentList.this;
        initialize();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cd = new ConnectionDetector(FareBreakUpPaymentList.this);
                isInternetPresent = cd.isConnectingToInternet();

                if (isInternetPresent) {
                    if (itemlist.get(position).getPaymentCode().equalsIgnoreCase("cash")) {
                        MakePayment_Cash(Iconstant.makepayment_cash_url);
                    } else if (itemlist.get(position).getPaymentCode().equalsIgnoreCase("wallet")) {
                        MakePayment_Wallet(Iconstant.makepayment_wallet_url);
                    } else if (itemlist.get(position).getPaymentCode().equalsIgnoreCase("auto_detect")) {
                        MakePayment_Stripe(Iconstant.makepayment_autoDetect_url);
                    } else {
                        MakePayment_WebView_MobileID(Iconstant.makepayment_Get_webview_mobileId_url);
                        SpaymentCode=itemlist.get(position).getPaymentCode();
                    }
                } else {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });

    }

    private void initialize() {
        session = new SessionManager(FareBreakUpPaymentList.this);
        cd = new ConnectionDetector(FareBreakUpPaymentList.this);
        isInternetPresent = cd.isConnectingToInternet();
        itemlist =new ArrayList<PaymentListPojo>();

        back = (RelativeLayout) findViewById(R.id.my_rides_payment_header_back_layout);
        listview = (ExpandableHeightListView) findViewById(R.id.my_rides_payment_listView);

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        UserID = user.get(SessionManager.KEY_USERID);

        Intent intent = getIntent();
        SrideId_intent = intent.getStringExtra("RideID");

        if (isInternetPresent) {
            postRequest_PaymentList(Iconstant.paymentList_url);
        }else {
            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
        }
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(FareBreakUpPaymentList.this);
        dialog.setTitle(title)
                .setMessage(alert)
                .setPositiveButton(
                        "OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        }
                )
                .show();
    }

    //method to convert currency code to currency symbol
    private static Locale getLocale(String strCode) {
        for (Locale locale : NumberFormat.getAvailableLocales()) {
            String code = NumberFormat.getCurrencyInstance(locale).getCurrency().getCurrencyCode();
            if (strCode.equals(code)) {
                return locale;
            }
        }
        return null;
    }



    //-----------------------PaymentList Post Request-----------------
    private void postRequest_PaymentList(String Url)
    {
        dialog = new Dialog(FareBreakUpPaymentList.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title=(TextView)dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_pleasewait));


        System.out.println("-------------PaymentList Url----------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------PaymentList Response----------------"+response);

                        String Sstatus = "";
                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if (Sstatus.equalsIgnoreCase("1")) {
                                JSONObject response_object = object.getJSONObject("response");
                                if (response_object.length() > 0) {
                                    JSONArray payment_array = response_object.getJSONArray("payment");
                                    if (payment_array.length() > 0) {
                                        itemlist.clear();
                                        for(int i=0;i<payment_array.length();i++)
                                        {
                                            JSONObject reason_object = payment_array.getJSONObject(i);
                                            PaymentListPojo pojo=new PaymentListPojo();
                                            pojo.setPaymentName(reason_object.getString("name"));
                                            pojo.setPaymentCode(reason_object.getString("code"));

                                            itemlist.add(pojo);
                                        }

                                        isPaymentAvailable=true;
                                    }
                                    else
                                    {
                                        isPaymentAvailable=false;
                                    }
                                }
                            }
                            else
                            {
                                String Sresponse = object.getString("response");
                                Alert(getResources().getString(R.string.alert_label_title), Sresponse);
                            }

                            if(Sstatus.equalsIgnoreCase("1") && isPaymentAvailable)
                            {
                                adapter = new MyRidePaymentListAdapter(FareBreakUpPaymentList.this, itemlist);
                                listview.setAdapter(adapter);
                                listview.setExpanded(true);
                            }

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(FareBreakUpPaymentList.this, error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",Iconstant.cabily_userAgent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_id", UserID);
                jsonParams.put("ride_id", SrideId_intent);
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }



    //-----------------------MakePayment Cash Post Request-----------------
    private void MakePayment_Cash(String Url) {
        dialog = new Dialog(FareBreakUpPaymentList.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_processing));


        System.out.println("-------------MakePayment Cash Url----------------" + Url);
        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------MakePayment Cash Response----------------" + response);

                        String Sstatus = "";
                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if (Sstatus.equalsIgnoreCase("1")) {

                                View view = View.inflate(FareBreakUpPaymentList.this, R.layout.material_alert_dialog, null);
                                final MaterialDialog mdialog = new MaterialDialog(FareBreakUpPaymentList.this);
                                mdialog.setContentView(view)
                                        .setPositiveButton(
                                                "OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        mdialog.dismiss();
                                                        finish();
                                                        FareBreakUp.farebreakup_class.finish();
                                                        onBackPressed();
                                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                    }
                                                }
                                        )
                                        .show();

                                TextView alert_title=(TextView)view.findViewById(R.id.material_alert_message_label);
                                TextView alert_message=(TextView)view.findViewById(R.id.material_alert_message_textview);
                                alert_title.setText(getResources().getString(R.string.my_rides_payment_cash_success));
                                alert_message.setText(getResources().getString(R.string.my_rides_payment_cash_driver_confirm_label));

                            } else {
                                String Sresponse = object.getString("response");
                                Alert(getResources().getString(R.string.alert_label_title), Sresponse);
                            }

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(FareBreakUpPaymentList.this, error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",Iconstant.cabily_userAgent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_id", UserID);
                jsonParams.put("ride_id", SrideId_intent);
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }





    //-----------------------MakePayment Wallet Post Request-----------------

    private void MakePayment_Wallet(String Url) {
        dialog = new Dialog(FareBreakUpPaymentList.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_processing));

        System.out.println("-------------MakePayment Wallet Url----------------" + Url);
        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------MakePayment Wallet Response----------------" + response);

                        String Sstatus = "",Scurrency_code="",Scurrent_wallet_balance="";
                        Currency currencycode = null;
                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if (Sstatus.equalsIgnoreCase("0")) {
                                final MaterialDialog dialog = new MaterialDialog(FareBreakUpPaymentList.this);
                                dialog.setTitle(getResources().getString(R.string.my_rides_payment_empty_wallet_sorry))
                                        .setMessage(getResources().getString(R.string.my_rides_payment_empty_wallet))
                                        .setPositiveButton(
                                                "OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();
                                                    }
                                                }
                                        )
                                        .show();
                            }
                            else if(Sstatus.equalsIgnoreCase("1"))
                            {
                                //Updating wallet amount on Navigation Drawer Slide
                                Scurrency_code = object.getString("currency");
                                currencycode = Currency.getInstance(getLocale(Scurrency_code));
                                Scurrent_wallet_balance = object.getString("wallet_amount");

                                session.createWalletAmount(currencycode.getSymbol()+Scurrent_wallet_balance);
                                NavigationDrawer.navigationNotifyChange();

                                final MaterialDialog dialog = new MaterialDialog(FareBreakUpPaymentList.this);
                                dialog.setTitle(getResources().getString(R.string.action_success))
                                        .setMessage(getResources().getString(R.string.my_rides_payment_wallet_success))
                                        .setPositiveButton(
                                                "OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();
                                                        finish();
                                                        FareBreakUp.farebreakup_class.finish();
                                                        Intent intent=new Intent(FareBreakUpPaymentList.this,MyRideRating.class);
                                                        intent.putExtra("RideID",SrideId_intent);
                                                        startActivity(intent);
                                                        overridePendingTransition(R.anim.enter,R.anim.exit);
                                                    }
                                                }
                                        )
                                        .show();
                            }
                            else if(Sstatus.equalsIgnoreCase("2"))
                            {
                                //Updating wallet amount on Navigation Drawer Slide
                                Scurrency_code = object.getString("currency");
                                currencycode = Currency.getInstance(getLocale(Scurrency_code));
                                Scurrent_wallet_balance = object.getString("wallet_amount");

                                session.createWalletAmount(currencycode.getSymbol()+Scurrent_wallet_balance);
                                NavigationDrawer.navigationNotifyChange();

                                Intent broadcastIntent = new Intent();
                                broadcastIntent.setAction("com.package.ACTION_CLASS_REFRESH");
                                sendBroadcast(broadcastIntent);

                                final MaterialDialog dialog = new MaterialDialog(FareBreakUpPaymentList.this);
                                dialog.setTitle(getResources().getString(R.string.my_rides_payment_cash_success))
                                        .setMessage(getResources().getString(R.string.my_rides_payment_cash_driver_confirm_label))
                                        .setPositiveButton(
                                                "OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();
                                                        postRequest_PaymentList(Iconstant.paymentList_url);
                                                    }
                                                }
                                        )
                                        .show();
                            }
                            else {
                                String Sresponse = object.getString("response");
                                Alert(getResources().getString(R.string.alert_label_title), Sresponse);
                            }

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(FareBreakUpPaymentList.this, error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",Iconstant.cabily_userAgent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_id", UserID);
                jsonParams.put("ride_id", SrideId_intent);
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }



    //-----------------------MakePayment Auto-Detect Post Request-----------------

    private void MakePayment_Stripe(String Url) {
        dialog = new Dialog(FareBreakUpPaymentList.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_processing));

        System.out.println("-------------MakePayment Auto-Detect Url----------------" + Url);
        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------MakePayment Auto-Detect Response----------------" + response);

                        String Sstatus = "";
                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if (Sstatus.equalsIgnoreCase("1")) {
                                final MaterialDialog dialog = new MaterialDialog(FareBreakUpPaymentList.this);
                                dialog.setTitle(getResources().getString(R.string.action_success))
                                        .setMessage(getResources().getString(R.string.my_rides_payment_cash_success))
                                        .setPositiveButton(
                                                "OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();
                                                        finish();
                                                        FareBreakUp.farebreakup_class.finish();
                                                        Intent intent=new Intent(FareBreakUpPaymentList.this,MyRideRating.class);
                                                        intent.putExtra("RideID",SrideId_intent);
                                                        startActivity(intent);
                                                        overridePendingTransition(R.anim.enter, R.anim.exit);
                                                    }
                                                }
                                        )
                                        .show();
                            }
                            else
                            {
                                String Sresponse = object.getString("response");
                                Alert(getResources().getString(R.string.alert_label_title), Sresponse);
                            }

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(FareBreakUpPaymentList.this, error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",Iconstant.cabily_userAgent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_id", UserID);
                jsonParams.put("ride_id", SrideId_intent);
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }





    //-----------------------MakePayment WebView-MobileID Post Request-----------------

    private void MakePayment_WebView_MobileID(String Url) {
        dialog = new Dialog(FareBreakUpPaymentList.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_processing));

        System.out.println("-------------MakePayment WebView-MobileID Url----------------" + Url);
        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------MakePayment WebView-MobileID Response----------------" + response);

                        String Sstatus = "";
                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if (Sstatus.equalsIgnoreCase("1")) {
                                String mobileId=object.getString("mobile_id");
                                Intent intent=new Intent(FareBreakUpPaymentList.this,FareBreakUpPaymentWebView.class);
                                intent.putExtra("MobileID",mobileId);
                                intent.putExtra("RideID",SrideId_intent);
                                startActivity(intent);
                                overridePendingTransition(R.anim.enter, R.anim.exit);
                            }
                            else
                            {
                                String Sresponse = object.getString("response");
                                Alert(getResources().getString(R.string.alert_label_title), Sresponse);
                            }

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(FareBreakUpPaymentList.this, error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",Iconstant.cabily_userAgent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_id", UserID);
                jsonParams.put("ride_id", SrideId_intent);
                jsonParams.put("gateway", SpaymentCode);
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }


    //-----------------Move Back on pressed phone back button------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {
            onBackPressed();
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            return true;
        }
        return false;
    }
}


