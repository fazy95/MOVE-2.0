package com.cabily.app;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
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
import com.cabily.utils.ConnectionDetector;
import com.casperon.app.cabily.R;
import com.mylibrary.volley.AppController;
import com.mylibrary.volley.VolleyErrorResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Prem Kumar and Anitha on 11/18/2015.
 */
public class ForgotPassword extends ActivityHockeyApp {
    private RelativeLayout back;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    StringRequest postRequest;
    Dialog dialog;

    private EditText Et_email;
    private Button Bt_submit;

    String Sstatus = "", Smessage = "",Ssms_status="",SverificationCode="",SemailAddress="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpassword);
        initialize();

        back.setOnClickListener(new View.OnClickListener() {
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


        Bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cd = new ConnectionDetector(ForgotPassword.this);
                isInternetPresent = cd.isConnectingToInternet();

                if (!isValidEmail(Et_email.getText().toString())) {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.forgot_password_email_label_enter_valid_email));
                } else {

                    if (isInternetPresent) {
                        PostRequest(Iconstant.forgot_password_url);
                    } else {
                        Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                    }
                }
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
    }

    private void initialize() {

        back = (RelativeLayout) findViewById(R.id.forgot_password_email_header_back_layout);
        Et_email = (EditText) findViewById(R.id.forgot_password_email_email_editText);
        Bt_submit = (Button) findViewById(R.id.forgot_password_email_submit_button);

        Bt_submit.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf"));
    }

    //-------------------------code to Check Email Validation-----------------------
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
        final MaterialDialog dialog = new MaterialDialog(ForgotPassword.this);
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

        dialog = new Dialog(ForgotPassword.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_verifying));

        postRequest = new StringRequest(Request.Method.POST, Url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                System.out.println("--------------Forgot Password reponse-------------------" + response);

                try {

                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    Smessage = object.getString("response");
                    if (Sstatus.equalsIgnoreCase("1")) {
                        Ssms_status = object.getString("sms_status");
                        SverificationCode = object.getString("verification_code");
                        SemailAddress = object.getString("email_address");
                    }


                    if (Sstatus.equalsIgnoreCase("1")) {
                        final MaterialDialog dialog = new MaterialDialog(ForgotPassword.this);
                        dialog.setTitle(getResources().getString(R.string.action_success))
                                .setMessage(getResources().getString(R.string.forgot_password_email_label_success))
                                .setPositiveButton(
                                        "OK", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                Intent i = new Intent(ForgotPassword.this, ForgotPasswordOtp.class);
                                                i.putExtra("Intent_Otp_Status",Ssms_status);
                                                i.putExtra("Intent_verificationCode",SverificationCode);
                                                i.putExtra("Intent_email",SemailAddress);
                                                startActivity(i);
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
                VolleyErrorResponse.volleyError(ForgotPassword.this, error);
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
                return jsonParams;
            }

        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postRequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postRequest);
    }

    //-----------------Move Back on pressed phone back button------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {

            // close keyboard
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(back.getWindowToken(), 0);

            onBackPressed();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
            return true;
        }
        return false;
    }

}
