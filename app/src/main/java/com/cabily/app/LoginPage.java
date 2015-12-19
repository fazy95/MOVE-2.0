package com.cabily.app;

/**
 * Created by Prem Kumar on 10/1/2015.
 */

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cabily.HockeyApp.ActivityHockeyApp;
import com.cabily.iconstant.Iconstant;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.mylibrary.pushnotification.GCMInitializer;
import com.mylibrary.volley.AppController;
import com.mylibrary.volley.VolleyErrorResponse;
import com.mylibrary.xmpp.ChatService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

public class LoginPage extends ActivityHockeyApp {
    private RelativeLayout back;
    private RelativeLayout forgotPwd;
    private EditText username, password;
    private Button submit;

    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private Context context;

    StringRequest postrequest;
    Dialog dialog;
    private SessionManager session;
    Handler mHandler;

    private String GCM_Id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginpage);
        context = getApplicationContext();
        initialize();
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // close keyboard
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(back.getWindowToken(), 0);
                onBackPressed();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });

        forgotPwd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginPage.this, ForgotPassword.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (username.getText().toString().length() == 0) {
                    erroredit(username, getResources().getString(R.string.login_label_alert_username));
                } else if (password.getText().toString().length() == 0) {
                    erroredit(password, getResources().getString(R.string.login_label_alert_password));
                } else {

                    cd = new ConnectionDetector(LoginPage.this);
                    isInternetPresent = cd.isConnectingToInternet();

                    if (isInternetPresent) {

                        mHandler.post(dialogRunnable);

                        //---------Getting GCM Id----------
                        GCMInitializer initializer = new GCMInitializer(LoginPage.this, new GCMInitializer.CallBack() {
                            @Override
                            public void onRegisterComplete(String registrationId) {

                                GCM_Id = registrationId;
                                PostRequest(Iconstant.loginurl);
                            }

                            @Override
                            public void onError(String errorMsg) {
                                PostRequest(Iconstant.loginurl);
                            }
                        });
                        initializer.init();

                    } else {
                        Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                    }
                }
            }
        });


        username.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(username);
                }
                return false;
            }
        });


        password.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(password);
                }
                return false;
            }
        });
    }

    private void initialize() {
        session = new SessionManager(LoginPage.this);
        cd = new ConnectionDetector(LoginPage.this);
        isInternetPresent = cd.isConnectingToInternet();
        mHandler = new Handler();

        back = (RelativeLayout) findViewById(R.id.login_header_back_layout);
        forgotPwd = (RelativeLayout) findViewById(R.id.login_forgotpwd_layout);
        username = (EditText) findViewById(R.id.login_email_editText);
        password = (EditText) findViewById(R.id.login_password_editText);
        submit = (Button) findViewById(R.id.login_submit_button);

        submit.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf"));

        //code to make password editText as dot
        password.setTransformationMethod(new PasswordTransformationMethod());

        username.addTextChangedListener(loginEditorWatcher);
        password.addTextChangedListener(loginEditorWatcher);
    }


    private void CloseKeyboard(EditText edittext) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(edittext.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(LoginPage.this);
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

    //----------------------Code for TextWatcher-------------------------
    private final TextWatcher loginEditorWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            //clear error symbol after entering text
            if (username.getText().length() > 0) {
                username.setError(null);
            }
            if (password.getText().length() > 0) {
                password.setError(null);
            }
        }
    };

    //--------------------Code to set error for EditText-----------------------
    private void erroredit(EditText editname, String msg) {
        Animation shake = AnimationUtils.loadAnimation(LoginPage.this, R.anim.shake);
        editname.startAnimation(shake);

        ForegroundColorSpan fgcspan = new ForegroundColorSpan(Color.parseColor("#CC0000"));
        SpannableStringBuilder ssbuilder = new SpannableStringBuilder(msg);
        ssbuilder.setSpan(fgcspan, 0, msg.length(), 0);
        editname.setError(ssbuilder);
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

    //--------Handler Method------------
    Runnable dialogRunnable = new Runnable() {
        @Override
        public void run() {
            dialog = new Dialog(LoginPage.this);
            dialog.getWindow();
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_loading);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    };


    // -------------------------code for Login Post Request----------------------------------

    private void PostRequest(final String Url) {

        postrequest = new StringRequest(Request.Method.POST, Url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                System.out.println("--------------Login reponse-------------------" + response);

                String Sstatus = "", Smessage = "", Suser_image = "", Suser_id = "", Suser_name = "",
                        Semail = "", Scountry_code = "", SphoneNo = "", Sreferal_code = "", Scategory = "", SsecretKey = "", SwalletAmount = "", ScurrencyCode = "";
                Currency currencycode = null;
                try {

                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    Smessage = object.getString("message");
                    if (Sstatus.equalsIgnoreCase("1")) {
                        Suser_image = object.getString("user_image");
                        Suser_id = object.getString("user_id");
                        Suser_name = object.getString("user_name");
                        Semail = object.getString("email");
                        Scountry_code = object.getString("country_code");
                        SphoneNo = object.getString("phone_number");
                        Sreferal_code = object.getString("referal_code");
                        Scategory = object.getString("category");
                        SsecretKey = object.getString("sec_key");
                        SwalletAmount = object.getString("wallet_amount");
                        ScurrencyCode = object.getString("currency");
                        currencycode = Currency.getInstance(getLocale(ScurrencyCode));
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (Sstatus.equalsIgnoreCase("1")) {
                    session.createLoginSession(Semail, Suser_id, Suser_name, Suser_image, Scountry_code, SphoneNo, Sreferal_code, Scategory);
                    session.createWalletAmount(currencycode.getSymbol() + SwalletAmount);
                    session.setXmppKey(Suser_id, SsecretKey);

                    //starting XMPP service
                    ChatService.startUserAction(LoginPage.this);
                    SingUpAndSignIn.activty.finish();
                    Intent intent = new Intent(context, NavigationDrawer.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                    finish();
                } else {
                    final MaterialDialog alertDialog = new MaterialDialog(LoginPage.this);
                    alertDialog.setTitle("Error");
                    alertDialog
                            .setMessage(Smessage)
                            .setCanceledOnTouchOutside(false)
                            .setPositiveButton(
                                    "OK", new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            alertDialog.dismiss();
                                        }
                                    }
                            ).show();
                }

                // close keyboard
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(username.getWindowToken(), 0);
                dialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(LoginPage.this, error);
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

                System.out.println("--------------Login url-------------------" + Url);
                System.out.println("--------------Login email-------------------" + username.getText().toString());
                System.out.println("--------------Login password-------------------" + password.getText().toString());
                System.out.println("--------------Login gcm_id-------------------" + GCM_Id);

                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("email", username.getText().toString());
                jsonParams.put("password", password.getText().toString());
                jsonParams.put("gcm_id", GCM_Id);
                return jsonParams;
            }

        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(postrequest);
    }



    //-----------------Move Back on pressed phone back button------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {

            // close keyboard
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(back.getWindowToken(), 0);

            LoginPage.this.finish();
            LoginPage.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return false;
    }

}
