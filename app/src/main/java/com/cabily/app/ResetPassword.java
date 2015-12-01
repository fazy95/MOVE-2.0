package com.cabily.app;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.cabily.utils.ConnectionDetector;
import com.casperon.app.cabily.R;
import com.mylibrary.volley.AppController;
import com.mylibrary.volley.VolleyErrorResponse;

import android.view.View.OnClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Prem Kumar and Anitha on 11/18/2015.
 */
public class ResetPassword extends ActivityHockeyApp {
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private RelativeLayout Rl_back;

    private EditText Et_email, Et_password;
    private Button Bt_send;
    private CheckBox Cb_showPwd;

    StringRequest postRequest;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resetpassword);
        initialize();

        Rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // close keyboard
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(Rl_back.getWindowToken(), 0);

                onBackPressed();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });

        Et_email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(Et_email);
                }
                return false;
            }
        });

        Et_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(Et_password);
                }
                return false;
            }
        });

        Cb_showPwd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (((CheckBox) v).isChecked()) {
                    Et_password.setTransformationMethod(null);
                } else {
                    Et_password.setTransformationMethod(new PasswordTransformationMethod());
                }

                Et_password.setSelection(Et_password.getText().length());
            }
        });

        Bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(ResetPassword.this);
                isInternetPresent = cd.isConnectingToInternet();

                if (!isValidEmail(Et_email.getText().toString())) {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.reset_password_email_label_enter_valid_email));
                } else if (Et_password.getText().toString().length() == 0) {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.reset_password_email_label_enter_valid_password));
                } else {
                    if (isInternetPresent) {
                        PostRequest(Iconstant.reset_password_url);
                    } else {
                        Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                    }
                }
            }
        });
    }

    private void initialize() {
        Rl_back = (RelativeLayout) findViewById(R.id.reset_password_header_back_layout);
        Et_email = (EditText) findViewById(R.id.reset_password_email_editText);
        Et_password = (EditText) findViewById(R.id.reset_password_password_editText);
        Bt_send = (Button) findViewById(R.id.reset_password_submit_button);
        Cb_showPwd = (CheckBox) findViewById(R.id.reset_password_show_password_checkBox);

        Et_password.setTransformationMethod(new PasswordTransformationMethod());
    }

    //----------------code to Check Email Validation----------
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    //--------------Close Keyboard Method-----------
    private void CloseKeyboard(EditText edittext) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(edittext.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(ResetPassword.this);
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


    // -------------------------code for Forgot Password Post Request----------------------------------

    private void PostRequest(final String Url) {

        dialog = new Dialog(ResetPassword.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));

        postRequest = new StringRequest(Request.Method.POST, Url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                System.out.println("--------------Reset Password reponse-------------------" + response);

                String Sstatus = "", Smessage = "";
                try {

                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    Smessage = object.getString("response");
                    if (Sstatus.equalsIgnoreCase("1")) {
                        final MaterialDialog dialog = new MaterialDialog(ResetPassword.this);
                        dialog.setTitle(getResources().getString(R.string.action_success))
                                .setMessage(Smessage)
                                .setPositiveButton(
                                        "OK", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                onBackPressed();
                                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                finish();
                                            }
                                        }
                                )
                                .show();
                    } else {
                        Alert(getResources().getString(R.string.action_error), Smessage);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // close keyboard
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(Et_email.getWindowToken(), 0);

                dialog.dismiss();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(ResetPassword.this, error);
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
                jsonParams.put("email", Et_email.getText().toString());
                jsonParams.put("password", Et_password.getText().toString());
                return jsonParams;
            }

        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postRequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postRequest);
    }


    //-----------------Move Back on pressed phone back button-------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {

            // close keyboard
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(Rl_back.getWindowToken(), 0);

            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return false;
    }
}
