package com.cabily.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.mylibrary.volley.AppController;
import com.mylibrary.volley.VolleyErrorResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Prem Kumar and Anitha on 10/8/2015.
 */
public class ProfileOtpPage extends ActivityHockeyApp
{
    private Context context;
    private	Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private SessionManager session;

    private RelativeLayout back;
    private EditText Eotp;
    private Button send;

    StringRequest postrequest;
    Dialog dialog;

    private String Suserid="",Sphone="",ScountryCode="";
    private String Sotp_Status="",Sotp="";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_otp_page);
        context = getApplicationContext();
        initialize();

        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // close keyboard
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(back.getWindowToken(), 0);

                onBackPressed();
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
            }
        });

        send.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if(Eotp.getText().toString().length()==0)
                {
                    erroredit(Eotp,getResources().getString(R.string.otp_label_alert_otp));
                }
                else if(!Sotp.equals(Eotp.getText().toString()))
                {
                    erroredit(Eotp,getResources().getString(R.string.otp_label_alert_invalid));
                }
                else
                {
                    if(isInternetPresent)
                    {
                        PostRequest(Iconstant.profile_edit_mobileNo_url);
                    }
                    else
                    {
                        Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                    }
                }
            }
        });


        Eotp.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(Eotp.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });
    }

    private void initialize()
    {
        session=new SessionManager(ProfileOtpPage.this);
        cd = new ConnectionDetector(ProfileOtpPage.this);
        isInternetPresent = cd.isConnectingToInternet();

        back=(RelativeLayout)findViewById(R.id.profile_otp_header_back_layout);
        Eotp=(EditText)findViewById(R.id.profile_otp_password_editText);
        send=(Button)findViewById(R.id.profile_otp_submit_button);

        Eotp.addTextChangedListener(EditorWatcher);

        Intent intent=getIntent();
        Suserid=intent.getStringExtra("UserID");
        Sphone=intent.getStringExtra("Phone");
        ScountryCode=intent.getStringExtra("CountryCode");
        Sotp_Status=intent.getStringExtra("Otp_Status");
        Sotp=intent.getStringExtra("Otp");

        if(Sotp_Status.equalsIgnoreCase("development"))
        {
            Eotp.setText(Sotp);
        }
        else
        {
            Eotp.setText("");
        }
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(ProfileOtpPage.this);
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
    private final TextWatcher EditorWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
        }
        @Override
        public void afterTextChanged(Editable s)
        {
            //clear error symbol after entering text
            if (Eotp.getText().length() > 0)
            {
                Eotp.setError(null);
            }
        }
    };

    //--------------------Code to set error for EditText-----------------------
    private void erroredit(EditText editname,String msg)
    {
        Animation shake = AnimationUtils.loadAnimation(ProfileOtpPage.this, R.anim.shake);
        editname.startAnimation(shake);

        ForegroundColorSpan fgcspan = new ForegroundColorSpan(Color.parseColor("#CC0000"));
        SpannableStringBuilder ssbuilder = new SpannableStringBuilder(msg);
        ssbuilder.setSpan(fgcspan, 0, msg.length(), 0);
        editname.setError(ssbuilder);
    }

    //-----------------Move Back on pressed phone back button------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {

            // close keyboard
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(back.getWindowToken(), 0);

            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return false;
    }


    // -----------------code for OTP Verification Post Request----------------
    private void PostRequest(String Url)
    {

        dialog = new Dialog(ProfileOtpPage.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title=(TextView)dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_otp));

        System.out.println("--------------Otp url-------------------"+Url);

        postrequest = new StringRequest(Request.Method.POST, Url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                System.out.println("--------------Otp reponse-------------------"+response);
                String Sstatus = "", Smessage = "",Scountry_code="",Sphone_number="";
                try {

                    JSONObject object=new JSONObject(response);

                    Sstatus = object.getString("status");
                    Smessage = object.getString("response");

                    if(Sstatus.equalsIgnoreCase("1"))
                    {
                        Scountry_code= object.getString("country_code");
                        Sphone_number= object.getString("phone_number");
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if(Sstatus.equalsIgnoreCase("1"))
                {
                    ProfilePage.updateMobileDialog(Scountry_code,Sphone_number);
                    session.setMobileNumberUpdate(Scountry_code, Sphone_number);

                    final MaterialDialog dialog = new MaterialDialog(ProfileOtpPage.this);
                    dialog.setTitle(getResources().getString(R.string.action_success))
                            .setMessage(getResources().getString(R.string.profile_lable_mobile_success))
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
                }
                else
                {
                    final MaterialDialog alertDialog= new MaterialDialog(ProfileOtpPage.this);
                    alertDialog.setTitle("Error");
                    alertDialog
                            .setMessage(Smessage)
                            .setCanceledOnTouchOutside(false)
                            .setPositiveButton(
                                    "OK", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            alertDialog.dismiss();
                                        }
                                    }
                            ).show();
                }

                // close keyboard
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(Eotp.getWindowToken(), 0);

                dialog.dismiss();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(ProfileOtpPage.this, error);
            }
        })  {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",Iconstant.cabily_userAgent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_id", Suserid);
                jsonParams.put("country_code", ScountryCode);
                jsonParams.put("phone_number", Sphone);
                jsonParams.put("otp", Sotp);
                return jsonParams;
            }
        };

        //to avoid repeat request Multiple Time
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        postrequest.setRetryPolicy(retryPolicy);
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(postrequest);
    }

}

