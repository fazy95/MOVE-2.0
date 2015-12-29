package com.cabily.app;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.StringRequest;
import com.cabily.HockeyApp.ActivityHockeyApp;
import com.cabily.utils.ConnectionDetector;
import com.casperon.app.cabily.R;
import com.mylibrary.dialog.PkDialog;


/**
 * Created by Prem Kumar and Anitha on 11/18/2015.
 */
public class ForgotPasswordOtp extends ActivityHockeyApp {
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private RelativeLayout Rl_back;
    private EditText Et_otp;
    private Button Bt_send;

    StringRequest postrequest;
    Dialog dialog;

    private String Semail = "", Sotp_Status = "", Sotp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpassword_otp);
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

        Et_otp.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(Et_otp.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });

        Bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Et_otp.getText().toString().length() == 0) {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.otp_label_alert_otp));
                } else if (!Sotp.equals(Et_otp.getText().toString())) {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.otp_label_alert_invalid));
                } else {
                    Intent i = new Intent(ForgotPasswordOtp.this, ResetPassword.class);
                    startActivity(i);
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            }
        });


    }

    private void initialize() {
        cd = new ConnectionDetector(ForgotPasswordOtp.this);
        isInternetPresent = cd.isConnectingToInternet();

        Rl_back = (RelativeLayout) findViewById(R.id.forgot_password_otp_header_back_layout);
        Et_otp = (EditText) findViewById(R.id.forgot_password_otp_password_editText);
        Bt_send = (Button) findViewById(R.id.forgot_password_otp_submit_button);

        Intent intent = getIntent();
        Semail = intent.getStringExtra("Intent_email");
        Sotp_Status = intent.getStringExtra("Intent_Otp_Status");
        Sotp = intent.getStringExtra("Intent_verificationCode");

        if (Sotp_Status.equalsIgnoreCase("development")) {
            Et_otp.setText(Sotp);
        } else {
            Et_otp.setText("");
        }

        Et_otp.setSelection(Et_otp.getText().length());
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {

        final PkDialog mDialog = new PkDialog(ForgotPasswordOtp.this);
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

    //-----------------Move Back on pressed phone back button------------------
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
