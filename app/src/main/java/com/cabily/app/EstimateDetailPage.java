package com.cabily.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cabily.HockeyApp.ActivityHockeyApp;
import com.cabily.pojo.EstimateDetailPojo;
import com.casperon.app.cabily.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Prem Kumar and Anitha on 10/9/2015.
 */
public class EstimateDetailPage extends ActivityHockeyApp
{
    private RelativeLayout Rl_ok;
    Context context;
    private TextView Tv_pickup, Tv_drop, Tv_priceRange, Tv_approxTime, Tv_note,Tv_peakTime,Tv_nightCharge;
    private ImageView rateCard;
    private String ScurrencyCode="",Spickup = "", Sdrop = "", SminPrice = "",SmaxPrice="", SapproxTime = "",SpeakTime="",SnightCharge="", Snote = "";
    ArrayList<EstimateDetailPojo> ratecard_list = new ArrayList<EstimateDetailPojo>();
    private boolean ratecard_clicked=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.estimation_details);
        context = getApplicationContext();
        initialize();

        rateCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ratecard_clicked)
                {
                    ratecard_clicked=false;
                    showRateCard();
                }

            }
        });

        Rl_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
    }

    private void initialize() {
        Tv_pickup = (TextView) findViewById(R.id.estimate_detail_pickup_textview);
        Tv_drop = (TextView) findViewById(R.id.estimate_detail_drop_textview);
        Tv_priceRange = (TextView) findViewById(R.id.estimate_detail_price_range_textview);
        Tv_approxTime = (TextView) findViewById(R.id.estimate_detail_price_approximate_textview);
        Tv_note = (TextView) findViewById(R.id.estimate_detail_note_textview);
        Tv_peakTime = (TextView) findViewById(R.id.estimate_detail_peektime_textview);
        Tv_nightCharge = (TextView) findViewById(R.id.estimate_detail_nightcharge_textview);
        rateCard = (ImageView) findViewById(R.id.estimate_detail_ratecard_imageView);
        Rl_ok = (RelativeLayout) findViewById(R.id.estimate_detail_ok_layout);

        Intent intent = getIntent();
        if (intent != null)
        {
            ScurrencyCode = intent.getStringExtra("CurrencyCode");
            Spickup = intent.getStringExtra("PickUp");
            Sdrop  = intent.getStringExtra("Drop");
            SminPrice  = intent.getStringExtra("MinPrice");
            SmaxPrice  = intent.getStringExtra("MaxPrice");
            SapproxTime  = intent.getStringExtra("ApproxPrice");
            SpeakTime  = intent.getStringExtra("PeakTime");
            SnightCharge  = intent.getStringExtra("NightCharge");
            Snote  = intent.getStringExtra("Note");

            EstimateDetailPojo data = (EstimateDetailPojo) getIntent().getSerializableExtra("RateCard");
            ratecard_list=data.getEstimatePojo();
        }

        Currency currencycode = Currency.getInstance(getLocale(ScurrencyCode));
        Tv_pickup.setText(Spickup);
        Tv_drop.setText(Sdrop);
        Tv_priceRange.setText(currencycode.getSymbol()+SminPrice+" "+"-"+" "+currencycode.getSymbol()+SmaxPrice);
        Tv_approxTime.setText(getResources().getString(R.string.estimate_detail_label_approx)+" "+SapproxTime);
        Tv_note.setText(Snote);

        if(SpeakTime.length()>0)
        {
            Tv_peakTime.setVisibility(View.VISIBLE);
            Tv_peakTime.setText(getResources().getString(R.string.estimate_detail_label_peekTime)+" "+Spickup);
        }
        else
        {
            Tv_peakTime.setVisibility(View.GONE);
        }

        if(SnightCharge.length()>0)
        {
            Tv_nightCharge.setVisibility(View.VISIBLE);
            Tv_nightCharge.setText(getResources().getString(R.string.estimate_detail_label_nightCharge)+" "+SnightCharge);
        }
        else
        {
            Tv_nightCharge.setVisibility(View.GONE);
        }

    }

    //-------------------Show RateCard Method--------------------
    private void showRateCard()
    {
        final MaterialDialog dialog = new MaterialDialog(EstimateDetailPage.this);
        View view = LayoutInflater.from(EstimateDetailPage.this).inflate(R.layout.ratecard_dialog, null);

        TextView tv_cartype = (TextView) view.findViewById(R.id.ratecard_caretype_textview);
        TextView tv_firstprice = (TextView) view.findViewById(R.id.first_price_textView);
        TextView tv_firstKm = (TextView) view.findViewById(R.id.first_km_textView);
        TextView tv_afterprice = (TextView) view.findViewById(R.id.after_price_textView);
        TextView tv_afterKm = (TextView) view.findViewById(R.id.after_km_textView);
        TextView tv_otherprice = (TextView) view.findViewById(R.id.other_price_textView);
        TextView tv_otherKm = (TextView) view.findViewById(R.id.other_km_textView);
        TextView tv_note = (TextView) view.findViewById(R.id.ratecard_note_textview);
        TextView tv_ok = (TextView) view.findViewById(R.id.ratecard_ok_textview);

        if (ratecard_list.size() > 0) {
            Currency currencycode = Currency.getInstance(getLocale(ratecard_list.get(0).getCurrencyCode()));

            tv_cartype.setText(ratecard_list.get(0).getRate_cartype());
            tv_firstprice.setText(currencycode.getSymbol() + ratecard_list.get(0).getMinfare_amt());
            tv_firstKm.setText(ratecard_list.get(0).getMinfare_km());
            tv_afterprice.setText(currencycode.getSymbol() + ratecard_list.get(0).getAfterfare_amt());
            tv_afterKm.setText(ratecard_list.get(0).getAfterfare_km());
            tv_otherprice.setText(currencycode.getSymbol() + ratecard_list.get(0).getOtherfare_amt());
            tv_otherKm.setText(ratecard_list.get(0).getOtherfare_km());
            tv_note.setText(ratecard_list.get(0).getRate_note());
        }

        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                ratecard_clicked=true;
            }
        });
        dialog.setView(view).show();
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

            // close keyboard
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(Rl_ok.getWindowToken(), 0);

            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return false;
    }
}
