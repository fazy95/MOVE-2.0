package com.cabily.app;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.cabily.HockeyApp.ActivityHockeyApp;
import com.cabily.iconstant.Iconstant;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.CurrencySymbolConverter;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.mylibrary.dialog.PkDialog;
import com.mylibrary.volley.ServiceRequest;
import com.mylibrary.xmpp.ChatService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;


/**
 * Created by Prem Kumar and Anitha on 10/20/2015.
 */
public class CabilyMoney extends ActivityHockeyApp {
    private RelativeLayout back;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private static Context context;
    private SessionManager session;
    private String UserID = "";

    private static TextView Tv_cabilymoney_current_balnce;
    private static Button Bt_cabilymoney_minimum_amount;
    private static Button Bt_cabilymoney_maximum_amount;
    private static Button Bt_cabilymoney_between_amount;
    private Button Bt_add_cabilymoney;
    private static EditText Et_cabilymoney_enteramount;
    private RelativeLayout layout_current_transaction;


    private ServiceRequest mRequest;
    Dialog dialog;
    private boolean isRechargeAvailable = false;
    private String Sauto_charge_status = "";
    private String Str_currentbalance = "", Str_minimum_amt = "", Str_maximum_amt = "", Str_midle_amt = "", ScurrencySymbol = "";

    public class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.package.ACTION_CLASS_CABILY_MONEY_REFRESH")) {
                if (isInternetPresent) {
                    postRequest_CabilyMoney(Iconstant.cabily_money_url);
                }
            }
        }
    }

    private RefreshReceiver refreshReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cabily_money);
        context = CabilyMoney.this;
        initialize();

        //Start XMPP Chat Service
        ChatService.startUserAction(CabilyMoney.this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // close keyboard
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(back.getWindowToken(), 0);

                onBackPressed();
                overridePendingTransition(R.anim.enter, R.anim.exit);
                finish();
            }
        });

        layout_current_transaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CabilyMoney.this, CabilyMoneyTransaction.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });

        Et_cabilymoney_enteramount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(Et_cabilymoney_enteramount);
                }
                return false;
            }
        });

        Bt_cabilymoney_minimum_amount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Et_cabilymoney_enteramount.setText(Str_minimum_amt);
                Bt_cabilymoney_minimum_amount.setBackgroundColor(0xFF009788);
                Bt_cabilymoney_between_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                Bt_cabilymoney_maximum_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                Et_cabilymoney_enteramount.setSelection(Et_cabilymoney_enteramount.getText().length());
            }
        });

        Bt_cabilymoney_between_amount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Et_cabilymoney_enteramount.setText(Str_midle_amt);
                Bt_cabilymoney_between_amount.setBackgroundColor(0xFF009788);
                Bt_cabilymoney_minimum_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                Bt_cabilymoney_maximum_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                Et_cabilymoney_enteramount.setSelection(Et_cabilymoney_enteramount.getText().length());
            }
        });

        Bt_cabilymoney_maximum_amount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Et_cabilymoney_enteramount.setText(Str_maximum_amt);
                Bt_cabilymoney_maximum_amount.setBackgroundColor(0xFF009788);
                Bt_cabilymoney_minimum_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                Bt_cabilymoney_between_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                Et_cabilymoney_enteramount.setSelection(Et_cabilymoney_enteramount.getText().length());
            }
        });

        Bt_add_cabilymoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredValue = Et_cabilymoney_enteramount.getText().toString();

                if (Str_minimum_amt != null && Str_minimum_amt.length() > 0) {
                    if (enteredValue.length() == 0) {
                        Alert(getResources().getString(R.string.action_error), getResources().getString(R.string.action_loading_cabily_money_empty_field));
                    } else if (Integer.parseInt(enteredValue) < Integer.parseInt(Str_minimum_amt) || Integer.parseInt(enteredValue) > Integer.parseInt(Str_maximum_amt)) {
                        Alert(getResources().getString(R.string.action_error), getResources().getString(R.string.cabilymoney_lable_rechargemoney_alert) + " " + ScurrencySymbol + Str_minimum_amt + " " + "-" + " " + ScurrencySymbol + Str_maximum_amt);
                    } else {
                        cd = new ConnectionDetector(CabilyMoney.this);
                        isInternetPresent = cd.isConnectingToInternet();

                        if (isInternetPresent) {
                            if (Sauto_charge_status.equalsIgnoreCase("1")) {
                                postRequest_AddMoney(Iconstant.cabily_add_money_url);
                            } else {
                                Intent intent = new Intent(CabilyMoney.this, CabilyMoneyWebview.class);
                                intent.putExtra("cabilyMoney_recharge_amount", Et_cabilymoney_enteramount.getText().toString());
                                intent.putExtra("cabilyMoney_currency_symbol", ScurrencySymbol);
                                intent.putExtra("cabilyMoney_currentBalance", Str_currentbalance);
                                startActivity(intent);
                                overridePendingTransition(R.anim.enter, R.anim.exit);
                            }
                        } else {
                            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                        }
                    }
                }
            }
        });

    }

    private void initialize() {
        session = new SessionManager(CabilyMoney.this);
        cd = new ConnectionDetector(CabilyMoney.this);
        isInternetPresent = cd.isConnectingToInternet();

        Bt_add_cabilymoney = (Button) findViewById(R.id.cabily_money_add_money_button);
        Et_cabilymoney_enteramount = (EditText) findViewById(R.id.cabily_money_enter_amount_edittext);
        Bt_cabilymoney_minimum_amount = (Button) findViewById(R.id.cabily_money_minimum_amt_button);
        Bt_cabilymoney_maximum_amount = (Button) findViewById(R.id.cabily_money_maximum_amt_button);
        Bt_cabilymoney_between_amount = (Button) findViewById(R.id.cabily_money_between_amt_button);
        Tv_cabilymoney_current_balnce = (TextView) findViewById(R.id.cabily_money_your_balance_textview);
        layout_current_transaction = (RelativeLayout) findViewById(R.id.cabily_money_current_balance_layout);
        back = (RelativeLayout) findViewById(R.id.cabily_money_header_back_layout);
        Et_cabilymoney_enteramount.addTextChangedListener(EditorWatcher);
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        UserID = user.get(SessionManager.KEY_USERID);
        // -----code to refresh drawer using broadcast receiver-----
        refreshReceiver = new RefreshReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.package.ACTION_CLASS_CABILY_MONEY_REFRESH");
        registerReceiver(refreshReceiver, intentFilter);
        if (isInternetPresent) {
            postRequest_CabilyMoney(Iconstant.cabily_money_url);
        } else {
            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
        }
    }

    //----------------------Code for TextWatcher-------------------------
    private final TextWatcher EditorWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            String strEnteredVal = Et_cabilymoney_enteramount.getText().toString();
            if (!strEnteredVal.equals("")) {
                if (Et_cabilymoney_enteramount.getText().toString().equals(Str_minimum_amt)) {
                    Bt_cabilymoney_minimum_amount.setBackgroundColor(0xFF009788);
                    Bt_cabilymoney_between_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                    Bt_cabilymoney_maximum_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                } else if (Et_cabilymoney_enteramount.getText().toString().equals(Str_midle_amt)) {
                    Bt_cabilymoney_between_amount.setBackgroundColor(0xFF009788);
                    Bt_cabilymoney_minimum_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                    Bt_cabilymoney_maximum_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                } else if (Et_cabilymoney_enteramount.getText().toString().equals(Str_maximum_amt)) {
                    Bt_cabilymoney_maximum_amount.setBackgroundColor(0xFF009788);
                    Bt_cabilymoney_minimum_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                    Bt_cabilymoney_between_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                } else {
                    Bt_cabilymoney_minimum_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                    Bt_cabilymoney_between_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                    Bt_cabilymoney_maximum_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                }
            }
        }
    };


    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final PkDialog mDialog = new PkDialog(CabilyMoney.this);
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

    //--------------Close KeyBoard Method-----------
    private void CloseKeyboard(EditText edittext) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(edittext.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }


    public static void changeButton() {
        Et_cabilymoney_enteramount.setText("");
        Bt_cabilymoney_minimum_amount.setBackground(context.getResources().getDrawable(R.drawable.grey_border_background));
        Bt_cabilymoney_between_amount.setBackground(context.getResources().getDrawable(R.drawable.grey_border_background));
        Bt_cabilymoney_maximum_amount.setBackground(context.getResources().getDrawable(R.drawable.grey_border_background));
    }


    //-----------------------Cabily Money Post Request-----------------
    private void postRequest_CabilyMoney(String Url) {
        dialog = new Dialog(CabilyMoney.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));


        System.out.println("-------------CabilyMoney Url----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);
        mRequest = new ServiceRequest(CabilyMoney.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("-------------CabilyMoney Response----------------" + response);

                String Sstatus = "", Scurrency_code = "", Scurrentbalance = "";

                try {
                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    Sauto_charge_status = object.getString("auto_charge_status");
                    if (Sstatus.equalsIgnoreCase("1")) {
                        JSONObject response_object = object.getJSONObject("response");
                        if (response_object.length() > 0) {
                            Scurrency_code = response_object.getString("currency");
                            Scurrentbalance = response_object.getString("current_balance");
                            Str_currentbalance = response_object.getString("current_balance");
                            ScurrencySymbol = CurrencySymbolConverter.getCurrencySymbol(Scurrency_code);

                            Object check_recharge_boundary_object = response_object.get("recharge_boundary");
                            if (check_recharge_boundary_object instanceof JSONObject) {
                                JSONObject recharge_object = response_object.getJSONObject("recharge_boundary");
                                if (recharge_object.length() > 0) {
                                    Str_minimum_amt = recharge_object.getString("min_amount");
                                    Str_maximum_amt = recharge_object.getString("max_amount");
                                    Str_midle_amt = recharge_object.getString("middle_amount");
                                    isRechargeAvailable = true;
                                } else {
                                    isRechargeAvailable = false;
                                }
                            }
                        }
                    } else {
                        isRechargeAvailable = false;
                    }


                    if (Sstatus.equalsIgnoreCase("1") && isRechargeAvailable) {
                        session.createWalletAmount(ScurrencySymbol + Str_currentbalance);
                        NavigationDrawer.navigationNotifyChange();

                        Bt_cabilymoney_minimum_amount.setText(ScurrencySymbol + Str_minimum_amt);
                        Bt_cabilymoney_maximum_amount.setText(ScurrencySymbol + Str_maximum_amt);
                        Bt_cabilymoney_between_amount.setText(ScurrencySymbol + Str_midle_amt);
                        Tv_cabilymoney_current_balnce.setText(ScurrencySymbol + Scurrentbalance);
                        Et_cabilymoney_enteramount.setHint(getResources().getString(R.string.cabilymoney_lable_rechargemoney_edittext_hint) + " " + ScurrencySymbol + Str_minimum_amt + " " + "-" + " " + ScurrencySymbol + Str_maximum_amt);
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

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });
    }


    //-----------------------Cabily Money Add Post Request-----------------
    private void postRequest_AddMoney(String Url) {
        dialog = new Dialog(CabilyMoney.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_processing));

        System.out.println("-------------Cabily ADD Money Url----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);
        jsonParams.put("total_amount", Et_cabilymoney_enteramount.getText().toString());

        mRequest = new ServiceRequest(CabilyMoney.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {
                System.out.println("-------------Cabily ADD Money Response----------------" + response);

                String Sstatus = "", Smessage = "", Swallet_money = "";

                try {
                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    Smessage = object.getString("msg");
                    Swallet_money = object.getString("wallet_amount");

                    if (Sstatus.equalsIgnoreCase("1")) {
                        session.createWalletAmount(ScurrencySymbol + Swallet_money);
                        NavigationDrawer.navigationNotifyChange();

                        Alert(getResources().getString(R.string.action_success), getResources().getString(R.string.action_loading_cabilymoney_transaction_wallet_success));
                        Et_cabilymoney_enteramount.setText("");
                        Tv_cabilymoney_current_balnce.setText(ScurrencySymbol + Swallet_money);
                        Bt_cabilymoney_minimum_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                        Bt_cabilymoney_between_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                        Bt_cabilymoney_maximum_amount.setBackground(getResources().getDrawable(R.drawable.grey_border_background));
                    } else {
                        Alert(getResources().getString(R.string.action_error), Smessage);
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

            // close keyboard
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(back.getWindowToken(), 0);

            onBackPressed();
            finish();
            overridePendingTransition(R.anim.enter, R.anim.exit);
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        // Unregister the logout receiver
        unregisterReceiver(refreshReceiver);
        super.onDestroy();
    }
}
