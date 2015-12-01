package com.cabily.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cabily.HockeyApp.ActivityHockeyApp;
import com.cabily.adapter.CabilyMoneyTransactionAdapter;
import com.cabily.iconstant.Iconstant;
import com.cabily.pojo.CabilyMoneyTransactionPojo;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
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
 * Created by Prem Kumar and Anitha on 10/22/2015.
 */
public class CabilyMoneyTransaction extends ActivityHockeyApp
{
    private RelativeLayout back;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private static Context context;
    private SessionManager session;
    private String UserID = "";

    private StringRequest postrequest;
    Dialog dialog;
    private boolean isTransactionAvailable=false;
    ArrayList<CabilyMoneyTransactionPojo> itemlist_all;
    ArrayList<CabilyMoneyTransactionPojo> itemlist_credit;
    ArrayList<CabilyMoneyTransactionPojo> itemlist_debit;
    CabilyMoneyTransactionAdapter adapter;

    private ListView listview;
    private TextView empty_text;
    private LinearLayout layout_All,layout_Credit,layout_Debit;
    private TextView Tv_All,Tv_Credit,Tv_Debit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cabily_money_transaction);
        context = getApplicationContext();
        initialize();


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.enter, R.anim.exit);
                finish();
            }
        });

        layout_All.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_All.setBackgroundColor(0xFF00897B);
                layout_Credit.setBackgroundColor(0xFFFFFFFF);
                layout_Debit.setBackgroundColor(0xFFFFFFFF);
                Tv_All.setTextColor(0xFFFFFFFF);
                Tv_Credit.setTextColor(0xFF00897B);
                Tv_Debit.setTextColor(0xFF00897B);

                adapter=new CabilyMoneyTransactionAdapter(CabilyMoneyTransaction.this,itemlist_all);
                listview.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                if(itemlist_all.size()>0)
                {
                    empty_text.setVisibility(View.GONE);
                }
                else
                {
                    empty_text.setVisibility(View.VISIBLE);
                    listview.setEmptyView(empty_text);
                }

            }
        });

        layout_Credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_All.setBackgroundColor(0xFFFFFFFF);
                layout_Credit.setBackgroundColor(0xFF00897B);
                layout_Debit.setBackgroundColor(0xFFFFFFFF);
                Tv_All.setTextColor(0xFF00897B);
                Tv_Credit.setTextColor(0xFFFFFFFF);
                Tv_Debit.setTextColor(0xFF00897B);

                adapter=new CabilyMoneyTransactionAdapter(CabilyMoneyTransaction.this,itemlist_credit);
                listview.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                if(itemlist_credit.size()>0)
                {
                    empty_text.setVisibility(View.GONE);
                }
                else
                {
                    empty_text.setVisibility(View.VISIBLE);
                    listview.setEmptyView(empty_text);
                }
            }
        });

        layout_Debit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_All.setBackgroundColor(0xFFFFFFFF);
                layout_Credit.setBackgroundColor(0xFFFFFFFF);
                layout_Debit.setBackgroundColor(0xFF00897B);
                Tv_All.setTextColor(0xFF00897B);
                Tv_Credit.setTextColor(0xFF00897B);
                Tv_Debit.setTextColor(0xFFFFFFFF);

                adapter=new CabilyMoneyTransactionAdapter(CabilyMoneyTransaction.this,itemlist_debit);
                listview.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                if(itemlist_debit.size()>0)
                {
                    empty_text.setVisibility(View.GONE);
                }
                else
                {
                    empty_text.setVisibility(View.VISIBLE);
                    listview.setEmptyView(empty_text);
                }
            }
        });
    }

    private void initialize() {
        session = new SessionManager(CabilyMoneyTransaction.this);
        cd = new ConnectionDetector(CabilyMoneyTransaction.this);
        isInternetPresent = cd.isConnectingToInternet();
        itemlist_all=new ArrayList<CabilyMoneyTransactionPojo>();
        itemlist_credit=new ArrayList<CabilyMoneyTransactionPojo>();
        itemlist_debit=new ArrayList<CabilyMoneyTransactionPojo>();

        back = (RelativeLayout) findViewById(R.id.cabily_money_transaction_header_back_layout);
        listview=(ListView)findViewById(R.id.cabily_money_transaction_listview);
        empty_text=(TextView)findViewById(R.id.cabily_money_transaction_listview_empty_text);
        layout_All=(LinearLayout)findViewById(R.id.cabily_money_transactions_all_layout);
        layout_Credit=(LinearLayout)findViewById(R.id.cabily_money_transactions_credits_layout);
        layout_Debit=(LinearLayout)findViewById(R.id.cabily_money_transactions_debit_layout);
        Tv_All=(TextView)findViewById(R.id.cabily_money_transactions_all_textview);
        Tv_Credit=(TextView)findViewById(R.id.cabily_money_transactions_credits_textview);
        Tv_Debit=(TextView)findViewById(R.id.cabily_money_transactions_debits_textview);

        layout_All.setBackgroundColor(0xFF00897B);
        Tv_All.setTextColor(0xFFFFFFFF);

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        UserID = user.get(SessionManager.KEY_USERID);

        if (isInternetPresent)
        {
            postRequest_CabilyMoney(Iconstant.cabily_money_transaction_url);
        }
        else
        {
            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
        }
    }


    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(CabilyMoneyTransaction.this);
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

    //-----------------------Cabily Money Post Request-----------------
    private void postRequest_CabilyMoney(String Url)
    {
        dialog = new Dialog(CabilyMoneyTransaction.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title=(TextView)dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));


        System.out.println("-------------CabilyMoney Transaction Url----------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------CabilyMoney Transaction Response----------------"+response);

                        String Sstatus = "";

                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");

                            if(Sstatus.equalsIgnoreCase("1"))
                            {
                                JSONObject response_object = object.getJSONObject("response");
                                if(response_object.length()>0)
                                {
                                    Currency currencycode = Currency.getInstance(getLocale(response_object.getString("currency")));

                                    JSONArray trans_array=response_object.getJSONArray("trans");
                                    if(trans_array.length()>0)
                                    {
                                        itemlist_all.clear();

                                        for (int i=0;i<trans_array.length();i++)
                                        {
                                            JSONObject trans_object = trans_array.getJSONObject(i);

                                            CabilyMoneyTransactionPojo pojo=new CabilyMoneyTransactionPojo();
                                            pojo.setTrans_type(trans_object.getString("type"));
                                            pojo.setTrans_amount(trans_object.getString("trans_amount"));
                                            pojo.setTitle(trans_object.getString("title"));
                                            pojo.setTrans_date(trans_object.getString("trans_date"));
                                            pojo.setBalance_amount(trans_object.getString("balance_amount"));
                                            pojo.setCurrencySymbol(currencycode.getSymbol());

                                            itemlist_all.add(pojo);

                                            if(trans_object.getString("type").equalsIgnoreCase("CREDIT"))
                                            {
                                                itemlist_credit.add(pojo);
                                            }
                                            else
                                            {
                                                itemlist_debit.add(pojo);
                                            }
                                        }
                                        isTransactionAvailable=true;
                                    }
                                    else
                                    {
                                        isTransactionAvailable=false;
                                    }
                                }

                            }



                            if(Sstatus.equalsIgnoreCase("1"))
                            {
                                if(isTransactionAvailable)
                                {
                                    empty_text.setVisibility(View.GONE);
                                    adapter=new CabilyMoneyTransactionAdapter(CabilyMoneyTransaction.this,itemlist_all);
                                    listview.setAdapter(adapter);
                                }
                                else
                                {
                                    empty_text.setVisibility(View.VISIBLE);
                                    listview.setEmptyView(empty_text);
                                }
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
                VolleyErrorResponse.volleyError(CabilyMoneyTransaction.this, error);
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
                jsonParams.put("type", "all");
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
            overridePendingTransition(R.anim.enter, R.anim.exit);
            return true;
        }
        return false;
    }
}
