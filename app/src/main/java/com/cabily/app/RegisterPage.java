package com.cabily.app;

/**
 * Created by Prem Kumar on 10/1/2015.
 */

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.volley.Request;
import com.cabily.HockeyApp.FragmentActivityHockeyApp;
import com.cabily.iconstant.Iconstant;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.CountryDialCode;
import com.casperon.app.cabily.R;
import com.countrycodepicker.CountryPicker;
import com.countrycodepicker.CountryPickerListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.mylibrary.dialog.PkDialog;
import com.mylibrary.gps.GPSTracker;
import com.mylibrary.pushnotification.GCMInitializer;
import com.mylibrary.volley.ServiceRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import me.drakeet.materialdialog.MaterialDialog;


public class RegisterPage extends FragmentActivityHockeyApp {
    private RelativeLayout back;
    private EditText Eusername, Epassword, Eemail, EphoneNo, Ereferalcode;
    private Button submit;
    private ImageView help;
    private RelativeLayout Rl_countryCode;
    private TextView Tv_countryCode;

    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private Context context;

    private ServiceRequest mRequest;
    Dialog dialog;
    Handler mHandler;
    //------------------GCM Initialization------------------
    private GoogleCloudMessaging gcm;
    private String GCM_Id = "";

    CountryPicker picker;
    private GPSTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registerpage);
        context = getApplicationContext();
        initialize();

        help.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Referral_information();
            }
        });

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

        Rl_countryCode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
            }
        });

        picker.setListener(new CountryPickerListener() {
            @Override
            public void onSelectCountry(String name, String code, String dialCode) {
                picker.dismiss();
                Tv_countryCode.setText(dialCode);

                // close keyboard
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(Rl_countryCode.getWindowToken(), 0);
            }
        });

        submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isValidEmail(Eemail.getText().toString())) {
                    erroredit(Eemail, getResources().getString(R.string.register_label_alert_email));
                } else if (!isValidPassword(Epassword.getText().toString())) {
                    erroredit(Epassword, getResources().getString(R.string.register_label_alert_password));
                } else if (Eusername.getText().toString().length() == 0) {
                    erroredit(Eusername, getResources().getString(R.string.register_label_alert_username));
                } else if (!isValidPhoneNumber(EphoneNo.getText().toString())) {
                    erroredit(EphoneNo, getResources().getString(R.string.register_label_alert_phoneNo));
                } else if (Tv_countryCode.getText().toString().equalsIgnoreCase("code")) {
                    erroredit(EphoneNo, getResources().getString(R.string.register_label_alert_country_code));
                } else {

                    cd = new ConnectionDetector(RegisterPage.this);
                    isInternetPresent = cd.isConnectingToInternet();

                    if (isInternetPresent) {

                        mHandler.post(dialogRunnable);

                        //---------Getting GCM Id----------
                        GCMInitializer initializer = new GCMInitializer(RegisterPage.this, new GCMInitializer.CallBack() {
                            @Override
                            public void onRegisterComplete(String registrationId) {

                                GCM_Id = registrationId;
                                PostRequest(Iconstant.register_url);
                            }

                            @Override
                            public void onError(String errorMsg) {
                                PostRequest(Iconstant.register_url);
                            }
                        });
                        initializer.init();

                    } else {
                        Alert(getResources().getString(R.string.alert_nointernet), getResources().getString(R.string.alert_nointernet_message));
                    }
                }
            }
        });


        Eusername.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(Eusername);
                }
                return false;
            }
        });


        Epassword.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(Epassword);
                }
                return false;
            }
        });

        Eemail.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(Eemail);
                }
                return false;
            }
        });


        EphoneNo.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(EphoneNo);
                }
                return false;
            }
        });
        Ereferalcode.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(Ereferalcode);
                }
                return false;
            }
        });


    }

    private void initialize() {
        cd = new ConnectionDetector(RegisterPage.this);
        isInternetPresent = cd.isConnectingToInternet();
        mHandler = new Handler();
        picker = CountryPicker.newInstance("Select Country");

        back = (RelativeLayout) findViewById(R.id.register_header_back_layout);
        Eusername = (EditText) findViewById(R.id.register_username_editText);
        Epassword = (EditText) findViewById(R.id.register_password_editText);
        Eemail = (EditText) findViewById(R.id.register_email_editText);
        EphoneNo = (EditText) findViewById(R.id.register_phone_editText);
        Ereferalcode = (EditText) findViewById(R.id.register_referalcode_editText);
        help = (ImageView) findViewById(R.id.register_referalcode_help_image);
        Rl_countryCode = (RelativeLayout) findViewById(R.id.register_country_code_layout);
        Tv_countryCode = (TextView) findViewById(R.id.register_country_code_textview);
        submit = (Button) findViewById(R.id.register_submit_button);
        submit.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf"));

        //code to make password editText as dot
        Epassword.setTransformationMethod(new PasswordTransformationMethod());

        Eusername.addTextChangedListener(loginEditorWatcher);
        Epassword.addTextChangedListener(loginEditorWatcher);


        gpsTracker = new GPSTracker(RegisterPage.this);
        if (gpsTracker.canGetLocation() && gpsTracker.isgpsenabled()) {

            double MyCurrent_lat = gpsTracker.getLatitude();
            double MyCurrent_long = gpsTracker.getLongitude();

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(MyCurrent_lat, MyCurrent_long, 1);
                if (addresses != null && !addresses.isEmpty()) {

                    String Str_getCountryCode = addresses.get(0).getCountryCode();
                    if (Str_getCountryCode.length() > 0 && !Str_getCountryCode.equals(null) && !Str_getCountryCode.equals("null")) {
                        String Str_countyCode = CountryDialCode.getCountryCode(Str_getCountryCode);
                        Tv_countryCode.setText(Str_countyCode);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void Referral_information() {
        final MaterialDialog dialog = new MaterialDialog(RegisterPage.this);
        View view = LayoutInflater.from(this).inflate(R.layout.register_referalcode_dialog, null);

        TextView tv_ok = (TextView) view.findViewById(R.id.referral_code_popup_text_ok);
        tv_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
        dialog.setView(view).show();
    }

    private void CloseKeyboard(EditText edittext) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(edittext.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {

        final PkDialog mDialog = new PkDialog(RegisterPage.this);
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
            if (Eusername.getText().length() > 0) {
                Eusername.setError(null);
            }
            if (Epassword.getText().length() > 0) {
                Epassword.setError(null);
            }
            if (Eemail.getText().length() > 0) {
                Eemail.setError(null);
            }
            if (EphoneNo.getText().length() > 0) {
                EphoneNo.setError(null);
            }

        }
    };


    //-------------------------code to Check Email Validation-----------------------
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    //--------------------Code to set error for EditText-----------------------
    private void erroredit(EditText editname, String msg) {
        Animation shake = AnimationUtils.loadAnimation(RegisterPage.this, R.anim.shake);
        editname.startAnimation(shake);

        ForegroundColorSpan fgcspan = new ForegroundColorSpan(Color.parseColor("#CC0000"));
        SpannableStringBuilder ssbuilder = new SpannableStringBuilder(msg);
        ssbuilder.setSpan(fgcspan, 0, msg.length(), 0);
        editname.setError(ssbuilder);
    }

    // validating password with retype password
    private boolean isValidPassword(String pass) {
        if (pass.length() < 6) {
            return false;
        }
            /*
             * else if(!pass.matches("(.*[A-Z].*)")) { return false; }
			 */
        else if (!pass.matches("(.*[a-z].*)")) {
            return false;
        } else if (!pass.matches("(.*[0-9].*)")) {
            return false;
        }
            /*
             * else if(!pass.matches(
			 * "(.*[,~,!,@,#,$,%,^,&,*,(,),-,_,=,+,[,{,],},|,;,:,<,>,/,?].*$)")) {
			 * return false; }
			 */
        else {
            return true;
        }

    }

    // validating Phone Number
    public static final boolean isValidPhoneNumber(CharSequence target) {
        if (target == null || TextUtils.isEmpty(target) || target.length() <= 9) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }
    }

    //--------Handler Method------------
    Runnable dialogRunnable = new Runnable() {
        @Override
        public void run() {
            dialog = new Dialog(RegisterPage.this);
            dialog.getWindow();
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_loading);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
            dialog_title.setText(getResources().getString(R.string.action_verifying));
        }
    };


    // -------------------------code for Login Post Request----------------------------------
    private void PostRequest(String Url) {

        System.out.println("--------------register url-------------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_name", Eusername.getText().toString());
        jsonParams.put("email", Eemail.getText().toString());
        jsonParams.put("password", Epassword.getText().toString());
        jsonParams.put("phone_number", EphoneNo.getText().toString());
        jsonParams.put("country_code", Tv_countryCode.getText().toString());
        jsonParams.put("referal_code", Ereferalcode.getText().toString());
        jsonParams.put("gcm_id", GCM_Id);

        mRequest = new ServiceRequest(RegisterPage.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                Log.e("registr",response);

                System.out.println("--------------register reponse-------------------" + response);

                String Sstatus = "", Smessage = "", Sotp_status = "", Sotp = "";

                try {

                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    Smessage = object.getString("message");

                    if (Sstatus.equalsIgnoreCase("1")) {
                        Sotp_status = object.getString("otp_status");
                        Sotp = object.getString("otp");
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                if (Sstatus.equalsIgnoreCase("1")) {
                    Intent intent = new Intent(context, OtpPage.class);
                    intent.putExtra("Otp_Status", Sotp_status);
                    intent.putExtra("Otp", Sotp);
                    intent.putExtra("UserName", Eusername.getText().toString());
                    intent.putExtra("Email", Eemail.getText().toString());
                    intent.putExtra("Password", Epassword.getText().toString());
                    intent.putExtra("Phone", EphoneNo.getText().toString());
                    intent.putExtra("CountryCode", Tv_countryCode.getText().toString());
                    intent.putExtra("ReferalCode", Ereferalcode.getText().toString());
                    intent.putExtra("GcmID", GCM_Id);

                    System.out.println("gcm---------"+GCM_Id);

                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                } else {
                    Alert(getResources().getString(R.string.login_label_alert_register_failed), Smessage);
                }

                // close keyboard
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(Eusername.getWindowToken(), 0);

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

            RegisterPage.this.finish();
            RegisterPage.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return false;
    }
}
