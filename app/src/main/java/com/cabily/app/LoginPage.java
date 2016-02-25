package com.cabily.app;

/**
 * Created by Prem Kumar on 10/1/2015.
 */

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.cabily.HockeyApp.ActivityHockeyApp;
import com.mylibrary.facebook.AsyncFacebookRunner;
import com.mylibrary.facebook.DialogError;
import com.mylibrary.facebook.Facebook;
import com.mylibrary.facebook.FacebookError;
import com.cabily.iconstant.Iconstant;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.CurrencySymbolConverter;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.mylibrary.dialog.PkDialog;
import com.mylibrary.pushnotification.GCMInitializer;
import com.mylibrary.volley.ServiceRequest;
import com.mylibrary.xmpp.ChatService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

public class LoginPage extends ActivityHockeyApp {
    private RelativeLayout back;
    private RelativeLayout forgotPwd;
    private EditText username, password;
    private Button submit;

    private Button facebooklayout;
    // Your FaceBook APP ID
    private static String APP_ID = "468945646630814";
    // Instance of FaceBook Class
    private final Facebook facebook = new Facebook(APP_ID);
    AsyncFacebookRunner mAsyncRunner;
    private SharedPreferences mPrefs;
    private String email = "", profile_image = "", username1 = "", userid = "";
    private JsonObjectRequest jsonObjReq;
    private StringRequest postrequest;

    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private Context context;
    private ServiceRequest mRequest;
    private Dialog dialog;
    private SessionManager session;
    private Handler mHandler;
    private String sCurrencySymbol = "";
    private String android_id;

    private String GCM_Id = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginpage);
        context = getApplicationContext();
        mAsyncRunner = new AsyncFacebookRunner(facebook);
        android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
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
                } else if (!isValidEmail(username.getText().toString())) {
                    erroredit(username, getResources().getString(R.string.login_label_alert_email_invalid));
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
                        Alert(getResources().getString(R.string.alert_nointernet), getResources().getString(R.string.alert_nointernet_message));
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

        facebooklayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loginToFacebook();
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

        facebooklayout = (Button) findViewById(R.id.login_facebook_button);

        submit.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf"));

        //code to make password editText as dot
        password.setTransformationMethod(new PasswordTransformationMethod());

        username.addTextChangedListener(loginEditorWatcher);
        password.addTextChangedListener(loginEditorWatcher);
    }


    //-------------------------code to Check Email Validation-----------------------
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }


    private void CloseKeyboard(EditText edittext) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(edittext.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final PkDialog mDialog = new PkDialog(LoginPage.this);
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

        System.out.println("-------GCM_Id-------" + GCM_Id);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("email", username.getText().toString());
        jsonParams.put("password", password.getText().toString());
        jsonParams.put("gcm_id", GCM_Id);

        mRequest = new ServiceRequest(LoginPage.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                Log.e("login", response);

                System.out.println("--------------Login reponse-------------------" + response);
                String Sstatus = "", Smessage = "", Suser_image = "", Suser_id = "", Suser_name = "",
                        Semail = "", Scountry_code = "", SphoneNo = "", Sreferal_code = "", Scategory = "", SsecretKey = "", SwalletAmount = "", ScurrencyCode = "";

                String is_alive_other = "";

                String gcmId = "";


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
                        gcmId = object.getString("key");
                        ScurrencyCode = object.getString("currency");
                        is_alive_other = object.getString("is_alive_other");
                        sCurrencySymbol = CurrencySymbolConverter.getCurrencySymbol(ScurrencyCode);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (Sstatus.equalsIgnoreCase("1")) {
                    session.createLoginSession(Semail, Suser_id, Suser_name, Suser_image, Scountry_code, SphoneNo, Sreferal_code, Scategory, gcmId);
                    session.createWalletAmount(sCurrencySymbol + SwalletAmount);
                    session.setXmppKey(Suser_id, SsecretKey);

                    System.out.println("insidesession gcm--------------" + gcmId);

                    if (is_alive_other.equalsIgnoreCase("Yes")) {
                        Alert(getResources().getString(R.string.alert_multiple_login), Smessage);
                    } else {
                        ChatService.startUserAction(LoginPage.this);
                        SingUpAndSignIn.activty.finish();
                        Intent intent = new Intent(context, UpdateUserLocation.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                    }

                  /*  //starting XMPP service
                    if("No".equalsIgnoreCase(is_alive_other)){
                        ChatService.startUserAction(LoginPage.this);
                        SingUpAndSignIn.activty.finish();
                        Intent intent = new Intent(context, UpdateUserLocation.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

                    }else{
                        Alert(getResources().getString(R.string.alert_multiple_login), Smessage);
                    }*/

                } else {
                    Alert(getResources().getString(R.string.login_label_alert_signIn_failed), Smessage);
                }
                // close keyboard
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(username.getWindowToken(), 0);
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

            LoginPage.this.finish();
            LoginPage.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return false;
    }


    //--------------------------------code for faceBook------------------------------

    public void loginToFacebook() {

        System.out.println("---------------facebook login1-----------------------");
        mPrefs = context.getSharedPreferences("CASPreferences", Context.MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);

        if (access_token != null) {
            facebook.setAccessToken(access_token);
        }


        System.out.println("---------------facebook expires-----------------------" + expires);

        if (expires != 0) {
            facebook.setAccessExpires(expires);
        }

        System.out.println("---------------facebook isSessionValid-----------------------" + facebook.isSessionValid());
        if (!facebook.isSessionValid()) {
            facebook.authorize(LoginPage.this,
                    new String[]{"email"},
                    new Facebook.DialogListener() {

                        @Override
                        public void onCancel() {
                            // Function to handle cancel event
                        }

                        @Override
                        public void onComplete(Bundle values) {
                            // Function to handle complete event
                            Toast.makeText(context, "logedin to app successfully", Toast.LENGTH_LONG).show();
                            // Edit Preferences and update facebook acess_token
                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.putString("access_token",
                                    facebook.getAccessToken());
                            editor.putLong("access_expires", facebook.getAccessExpires());
                            editor.commit();
                            String accessToken = facebook.getAccessToken();
                            PostRequest_facebook(Iconstant.social_check_url);
                        }

                        @Override
                        public void onError(DialogError error) {
                            // Function to handle error

                        }

                        @Override
                        public void onFacebookError(FacebookError fberror) {
                            // Function to handle Facebook errors

                        }
                    });
        }
    }

    public void getProfileInformation() {
        mAsyncRunner.request("me", new AsyncFacebookRunner.RequestListener() {
            @Override
            public void onComplete(String response, Object state) {
                Log.e("Profile", response);
                String json = response;
                try {
                    // Facebook Profile JSON data
                    JSONObject profile = new JSONObject(json);

                    // getting name of the user
                    final String name = profile.getString("name");

                    // getting email of the user
                    final String email = profile.getString("email");

                    LoginPage.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(LoginPage.this, "Name: " + name + "\nEmail: " + email, Toast.LENGTH_LONG).show();
                        }

                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onIOException(IOException e, Object state) {
            }

            @Override
            public void onFileNotFoundException(FileNotFoundException e,
                                                Object state) {
            }

            @Override
            public void onMalformedURLException(MalformedURLException e,
                                                Object state) {
            }

            @Override
            public void onFacebookError(FacebookError e, Object state) {
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebook.authorizeCallback(requestCode, resultCode, data);
    }

    public void logoutFromFacebook() {
        mAsyncRunner.logout(this, new AsyncFacebookRunner.RequestListener() {

            @Override
            public void onComplete(String response, Object state) {
                System.out.println("----------facebook response---------------" + response);
                if (Boolean.parseBoolean(response) == true) {
                    // User successfully Logged out
                    System.out.println("-----------facebook logout---------------");
                }
            }

            @Override
            public void onIOException(IOException e, Object state) {
            }

            @Override
            public void onFileNotFoundException(FileNotFoundException e,
                                                Object state) {
            }

            @Override
            public void onMalformedURLException(MalformedURLException e,
                                                Object state) {
            }

            @Override
            public void onFacebookError(FacebookError e, Object state) {
            }
        });
    }


    private void PostRequest_facebook(final String Url) {


        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("media_id", APP_ID);
        jsonParams.put("deviceToken", "");
        jsonParams.put("gcm_id", GCM_Id);

        mRequest = new ServiceRequest(LoginPage.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("--------------Login reponse-------------------" + response);

                String Sstatus = "", Smessage = "", Suser_image = "", Suser_id = "", Suser_name = "",
                        Semail = "", Scountry_code = "", SphoneNo = "", Sreferal_code = "", Scategory = "", SsecretKey = "", SwalletAmount = "", ScurrencyCode = "";

                String gcmId = "";
                String is_alive_other = "";

                try {

                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    Smessage = object.getString("message");
                    System.out.println("---------Sstatus--------" + Sstatus);
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

                        gcmId = object.getString("key");
                        is_alive_other = object.getString("is_alive_other");

                        ScurrencyCode = object.getString("currency");
                        sCurrencySymbol = CurrencySymbolConverter.getCurrencySymbol(ScurrencyCode);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (Sstatus.equalsIgnoreCase("1")) {
                    session.createLoginSession(Semail, Suser_id, Suser_name, Suser_image, Scountry_code, SphoneNo, Sreferal_code, Scategory, gcmId);
                    session.createWalletAmount(sCurrencySymbol + SwalletAmount);
                    session.setXmppKey(Suser_id, SsecretKey);

                    //starting XMPP service
                    ChatService.startUserAction(LoginPage.this);
                    SingUpAndSignIn.activty.finish();
                    Intent intent = new Intent(context, UpdateUserLocation.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else if (Sstatus.equalsIgnoreCase("2")) {

                    String accessToken = facebook.getAccessToken();
                    JsonRequest("https://graph.facebook.com/me?fields=id,name,picture,email&access_token=" + accessToken);

                } else {
                    Alert(getResources().getString(R.string.login_label_alert_signIn_failed), Smessage);
                }

                // close keyboard
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(username.getWindowToken(), 0);
                dialog.dismiss();
            }

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });
    }

    private void JsonRequest(final String Url) {


        final ProgressDialog progress;
        progress = new ProgressDialog(LoginPage.this);
        progress.setMessage("Please Wait...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(false);
        progress.show();

        HashMap<String, String> jsonParams = new HashMap<String, String>();

        mRequest = new ServiceRequest(LoginPage.this);
        mRequest.makeServiceRequest(Url, Request.Method.GET, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("--------------access token reponse-------------------" + response);

                try {

                    JSONObject object = new JSONObject(response);
                    System.out.println("---------facebook profile------------" + response);


                    userid = object.getString("id");
                    profile_image = "https://graph.facebook.com/" + object.getString("id") + "/picture?type=large";
                    username1 = object.getString("name");
                    username1 = username1.replaceAll("\\s+", "");


                    if (object.has("email")) {
                        email = object.getString("email");
                    } else {
                        email = "";
                    }
                    System.out.println("-------email------------------" + email);
                    System.out.println("-----------------userid-------------------------------" + userid);
                    System.out.println("----------------profile_image-----------------" + profile_image);
                    System.out.println("-----------username----------" + username1);


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //post execute
                progress.dismiss();

                Intent intent = new Intent(LoginPage.this, RegisterFacebook.class);
                intent.putExtra("userId", userid);
                intent.putExtra("userName", username1);
                intent.putExtra("userEmail", email);
                intent.putExtra("media", "facebook");
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);


                // close keyboard
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(username.getWindowToken(), 0);

            }

            @Override
            public void onErrorListener() {
                progress.dismiss();
            }
        });
    }
}
