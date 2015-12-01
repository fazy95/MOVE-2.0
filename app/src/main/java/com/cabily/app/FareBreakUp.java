package com.cabily.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cabily.HockeyApp.ActivityHockeyApp;
import com.cabily.subclass.ActivitySubClass;
import com.cabily.utils.ConnectionDetector;
import com.casperon.app.cabily.R;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

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
    }

    private void initialize() {

        Tv_totalAmount=(TextView)findViewById(R.id.fare_breakup_total_amount_textview);
        Tv_duration=(TextView)findViewById(R.id.fare_breakup_duration_textview);
        Tv_waiting=(TextView)findViewById(R.id.fare_breakup_waiting_textview);
        Tv_timeTravel=(TextView)findViewById(R.id.fare_breakup_timetravel_textview);
        Rl_payment=(RelativeLayout)findViewById(R.id.fare_breakup_payment_layout);

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
