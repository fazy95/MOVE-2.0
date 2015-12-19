package com.cabily.app;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cabily.HockeyApp.ActivityHockeyApp;
import com.cabily.iconstant.Iconstant;
import com.cabily.subclass.ActivitySubClass;
import com.cabily.utils.ConnectionDetector;
import com.casperon.app.cabily.R;
import com.mylibrary.volley.AppController;
import com.mylibrary.volley.VolleyErrorResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Prem Kumar and Anitha on 11/7/2015.
 */
public class FareBreakUp extends ActivitySubClass
{
    private TextView Tv_totalAmount,Tv_duration,Tv_waiting,Tv_timeTravel;
    private RelativeLayout Rl_payment;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private String SrideId_intent="",ScurrencyCode="",StotalAmount="",Sduation="",SwaitingTime="",StravelDistance="";
    Currency currencycode = null;

    public static FareBreakUp farebreakup_class;

    //------Tip Declaration-----
    private EditText Et_tip_Amount;
    private Button Bt_tip_Apply;
    private RelativeLayout Rl_tip;

    private StringRequest postrequest;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.farebreak_up);
        farebreakup_class=FareBreakUp.this;
        initialize();

        Rl_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(FareBreakUp.this);
                isInternetPresent = cd.isConnectingToInternet();

                if (isInternetPresent) {
                    Intent passIntent = new Intent(FareBreakUp.this, FareBreakUpPaymentList.class);
                    passIntent.putExtra("RideID", SrideId_intent);
                    startActivity(passIntent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });


        Bt_tip_Apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(FareBreakUp.this);
                isInternetPresent = cd.isConnectingToInternet();

                if(Et_tip_Amount.getText().toString().length()>0)
                {
                    if (isInternetPresent) {
                        if(Bt_tip_Apply.getText().toString().equalsIgnoreCase(getResources().getString(R.string.my_rides_detail_tip_apply_label)))
                        {
                            postRequest_Tip(Iconstant.tip_add_url,"Apply");
                        }
                        else if(Bt_tip_Apply.getText().toString().equalsIgnoreCase(getResources().getString(R.string.my_rides_detail_tip_remove_label)))
                        {
                            postRequest_Tip(Iconstant.tip_remove_url,"Remove");
                        }
                    } else {
                        Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                    }
                }
                else {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.my_rides_detail_tip_empty_label));
                }

            }
        });
    }

    private void initialize() {

        Tv_totalAmount=(TextView)findViewById(R.id.fare_breakup_total_amount_textview);
        Tv_duration=(TextView)findViewById(R.id.fare_breakup_duration_textview);
        Tv_waiting=(TextView)findViewById(R.id.fare_breakup_waiting_textview);
        Tv_timeTravel=(TextView)findViewById(R.id.fare_breakup_timetravel_textview);
        Rl_payment=(RelativeLayout)findViewById(R.id.fare_breakup_payment_layout);

        Et_tip_Amount =(EditText)findViewById(R.id.fare_breakup_tip_editText);
        Bt_tip_Apply =(Button)findViewById(R.id.fare_breakup_tip_apply_button);
        Rl_tip =(RelativeLayout)findViewById(R.id.fare_breakup_tip_layout);

        Intent intent=getIntent();
        SrideId_intent=intent.getStringExtra("RideID");
        ScurrencyCode=intent.getStringExtra("CurrencyCode");
        StotalAmount=intent.getStringExtra("TotalAmount");
        StravelDistance=intent.getStringExtra("TravelDistance");
        Sduation=intent.getStringExtra("Duation");
        SwaitingTime=intent.getStringExtra("WaitingTime");

        currencycode = Currency.getInstance(getLocale(ScurrencyCode));

        Tv_totalAmount.setText(currencycode.getSymbol()+StotalAmount);
        Tv_duration.setText(Sduation);
        Tv_waiting.setText(SwaitingTime);
        Tv_timeTravel.setText(StravelDistance);
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(FareBreakUp.this);
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



    //-----------------------Tip Post Request-----------------
    private void postRequest_Tip(String Url, final String tipStatus) {
        dialog = new Dialog(FareBreakUp.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_pleasewait));


        System.out.println("-------------tip Url----------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------tip Response----------------" + response);
                        String Sstatus = "", Sresponse = "";
                        try {

                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if (Sstatus.equalsIgnoreCase("1")) {

                                JSONObject response_Object=object.getJSONObject("response");
                                Sresponse = response_Object.getString("msg");

                                if(tipStatus.equalsIgnoreCase("Apply"))
                                {
                                    Bt_tip_Apply.setText(getResources().getString(R.string.my_rides_detail_tip_remove_label));
                                    Bt_tip_Apply.setBackgroundColor(0xFFCC0000);
                                    Et_tip_Amount.setEnabled(false);
                                }
                                else
                                {
                                    Bt_tip_Apply.setText(getResources().getString(R.string.my_rides_detail_tip_apply_label));
                                    Bt_tip_Apply.setBackgroundColor(0xFF01A7CD);
                                    Et_tip_Amount.setEnabled(true);
                                    Et_tip_Amount.setText("");
                                }
                                Alert(getResources().getString(R.string.action_success), Sresponse);
                            } else {
                                Alert(getResources().getString(R.string.alert_label_title), Sresponse);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(FareBreakUp.this, error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent", Iconstant.cabily_userAgent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("ride_id", SrideId_intent);

                if(tipStatus.equalsIgnoreCase("Apply"))
                {
                    jsonParams.put("tips_amount", Et_tip_Amount.getText().toString());
                }
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
            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.fare_breakup_label_complete_payment));
            return true;
        }
        return false;
    }

}
