package com.cabily.app;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.cabily.iconstant.Iconstant;
import com.cabily.subclass.ActivitySubClass;
import com.cabily.utils.ConnectionDetector;
import com.casperon.app.cabily.R;
import com.mylibrary.dialog.PkDialog;
import com.mylibrary.volley.ServiceRequest;
import com.mylibrary.widgets.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;


/**
 * Created by Prem Kumar and Anitha on 11/7/2015.
 */
public class FareBreakUp extends ActivitySubClass {
    private TextView Tv_baseFare, Tv_duration, Tv_waiting, Tv_timeTravel;
    private RelativeLayout Rl_payment;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private String SrideId_intent = "", ScurrencyCode = "", StotalAmount = "", Sduation = "", SwaitingTime = "", StravelDistance = "";
    Currency currencycode = null;
    private RoundedImageView Im_DriverImage;
    private TextView Tv_DriverName, Tv_SubTotal, Tv_TripTotal;

    public static FareBreakUp farebreakup_class;

    //------Tip Declaration-----
    private EditText Et_tip_Amount;
    private Button Bt_tip_Apply;
    private RelativeLayout Rl_tip;
    private TextView Tv_tip;
    private LinearLayout Ll_TipAmount;
    private LinearLayout Ll_RemoveTip;
    private RelativeLayout Rl_TipMain;
    private String sSelectedTipAmount = "";
    private RatingBar Rb_driver;
    private TextView Tv_serviceTax;

    private String sDriverName = "", sDriverImage = "", sDriverRating = "", sDriverLat = "", sDriverLong = "",
            sUserLat = "", sUserLong = "", sSubTotal = "", sServiceTax = "", sTotalPayment = "";

    private ServiceRequest mRequest;
    private CheckBox Cb_tip;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.farebreak_up);
        farebreakup_class = FareBreakUp.this;
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

                if (Et_tip_Amount.getText().toString().length() > 0) {
                    if (isInternetPresent) {
                        postRequest_Tip(Iconstant.tip_add_url, "Apply");
                    } else {
                        Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                    }
                } else {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.my_rides_detail_tip_empty_label));
                }

            }
        });


        Ll_RemoveTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cd = new ConnectionDetector(FareBreakUp.this);
                isInternetPresent = cd.isConnectingToInternet();

                if (isInternetPresent) {
                    postRequest_Tip(Iconstant.tip_remove_url, "Remove");
                } else {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });


        Cb_tip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    Rl_tip.setVisibility(View.VISIBLE);
                } else {
                    Rl_tip.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initialize() {

        Tv_baseFare = (TextView) findViewById(R.id.fare_breakup_total_amount_textview);
        Tv_duration = (TextView) findViewById(R.id.fare_breakup_duration_textview);
        Tv_waiting = (TextView) findViewById(R.id.fare_breakup_waiting_textview);
        Tv_timeTravel = (TextView) findViewById(R.id.fare_breakup_timetravel_textview);
        Rl_payment = (RelativeLayout) findViewById(R.id.fare_breakup_payment_layout);

        Im_DriverImage = (RoundedImageView) findViewById(R.id.fare_breakup_imageview);
        Tv_DriverName = (TextView) findViewById(R.id.fare_breakup_driver_name_textView);
        Tv_SubTotal = (TextView) findViewById(R.id.fare_breakup_subtotal_textView);
        Tv_TripTotal = (TextView) findViewById(R.id.fare_breakup_trip_total_textView);

        Et_tip_Amount = (EditText) findViewById(R.id.fare_breakup_tip_editText);
        Bt_tip_Apply = (Button) findViewById(R.id.fare_breakup_tip_apply_button);
        Rl_tip = (RelativeLayout) findViewById(R.id.fare_breakup_tip_layout);
        Cb_tip = (CheckBox) findViewById(R.id.fare_breakup_tip_checkBox);

        Tv_tip = (TextView) findViewById(R.id.fare_breakup_tip_amount_textView);
        Ll_TipAmount = (LinearLayout) findViewById(R.id.fare_breakup_tip_amount_layout);
        Ll_RemoveTip = (LinearLayout) findViewById(R.id.fare_breakup_tip_amount_remove_layout);
        Rl_TipMain = (RelativeLayout) findViewById(R.id.fare_breakup_tip_top_layout);
        Rb_driver =(RatingBar) findViewById(R.id.fare_breakup_driver_ratingBar);
        Tv_serviceTax =(TextView) findViewById(R.id.fare_breakup_serviceTax_textView);

        Intent intent = getIntent();
        SrideId_intent = intent.getStringExtra("RideID");
        ScurrencyCode = intent.getStringExtra("CurrencyCode");
        StotalAmount = intent.getStringExtra("TotalAmount");
        StravelDistance = intent.getStringExtra("TravelDistance");
        Sduation = intent.getStringExtra("Duration");
        SwaitingTime = intent.getStringExtra("WaitingTime");
        sDriverName = intent.getStringExtra("DriverName");
        sDriverImage = intent.getStringExtra("DriverImage");
        sDriverRating = intent.getStringExtra("DriverRating");
        sDriverLat = intent.getStringExtra("DriverLatitude");
        sDriverLong = intent.getStringExtra("DriverLongitude");
        sUserLat = intent.getStringExtra("UserLatitude");
        sUserLong = intent.getStringExtra("UserLongitude");
        sSubTotal = intent.getStringExtra("SubTotal");
        sServiceTax = intent.getStringExtra("ServiceTax");
        sTotalPayment = intent.getStringExtra("TotalPayment");

        currencycode = Currency.getInstance(getLocale(ScurrencyCode));

        Picasso.with(FareBreakUp.this).invalidate(sDriverImage);
        Picasso.with(FareBreakUp.this).load(sDriverImage).into(Im_DriverImage);
        Tv_DriverName.setText(sDriverName);
        if(sDriverRating.length()>0)
        {
            Rb_driver.setRating(Float.parseFloat(sDriverRating));
        }

        Tv_baseFare.setText(currencycode.getSymbol() + StotalAmount);
        Tv_duration.setText(Sduation);
        Tv_waiting.setText(SwaitingTime);
        Tv_timeTravel.setText(StravelDistance);
        Tv_SubTotal.setText(currencycode.getSymbol() + sSubTotal);
        Tv_serviceTax.setText(currencycode.getSymbol() + sServiceTax);
        Tv_TripTotal.setText(currencycode.getSymbol() + sTotalPayment);

    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {

        final PkDialog mDialog = new PkDialog(FareBreakUp.this);
        mDialog.setDialogTitle(title);
        mDialog.setDialogMessage(alert);
        mDialog.setPositiveButton(getResources().getString(R.string.action_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
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


        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("ride_id", SrideId_intent);
        if (tipStatus.equalsIgnoreCase("Apply")) {
            jsonParams.put("tips_amount", Et_tip_Amount.getText().toString());
        }

        mRequest = new ServiceRequest(FareBreakUp.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                String sStatus = "", sResponse = "",sTipAmount="";
                try {

                    JSONObject object = new JSONObject(response);
                    sStatus = object.getString("status");
                    if (sStatus.equalsIgnoreCase("1")) {

                        JSONObject response_Object = object.getJSONObject("response");
                        sTipAmount = response_Object.getString("tips_amount");
                        sTotalPayment = response_Object.getString("total");
                        if (tipStatus.equalsIgnoreCase("Apply")) {
                            sSelectedTipAmount = sTipAmount;
                            Tv_tip.setText(currencycode.getSymbol() + sTipAmount);
                            Tv_TripTotal.setText(currencycode.getSymbol() + sTotalPayment);
                            Rl_TipMain.setVisibility(View.GONE);
                            Rl_tip.setVisibility(View.GONE);
                            Ll_TipAmount.setVisibility(View.VISIBLE);
                        } else {
                            Tv_TripTotal.setText(currencycode.getSymbol() + sTotalPayment);
                            Cb_tip.setChecked(false);
                            Et_tip_Amount.setText("");
                            Rl_TipMain.setVisibility(View.VISIBLE);
                            Ll_TipAmount.setVisibility(View.GONE);
                        }

                    } else {
                         sResponse = object.getString("response");
                         Alert(getResources().getString(R.string.alert_label_title), sResponse);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();
            }

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });
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
