package com.cabily.app;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.cabily.HockeyApp.FragmentActivityHockeyApp;
import com.cabily.iconstant.Iconstant;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.countrycodepicker.CountryPicker;
import com.countrycodepicker.CountryPickerListener;
import com.mylibrary.dialog.PkDialog;
import com.mylibrary.volley.ServiceRequest;
import com.mylibrary.xmpp.ChatService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


/**
 * Created by Prem Kumar on 10/7/2015.
 */
public class ProfilePage extends FragmentActivityHockeyApp {

    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private Context context;
    private SessionManager session;
    private RelativeLayout layout_changePassword, back;
    private Button logout;
    private TextView tv_email;
    private EditText Et_name;
    private static EditText Et_mobileno;
    private static RelativeLayout Rl_country_code;
    private static TextView Tv_countryCode;
    private String UserID = "", UserName = "", UserMobileno = "", UserCountyCode = "", UserEmail = "";
    private ServiceRequest mRequest;
    Dialog dialog;
    CountryPicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profilepage);
        context = getApplicationContext();
        initialize();

        //Start XMPP Chat Service
        ChatService.startUserAction(ProfilePage.this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.enter, R.anim.exit);
                finish();
            }
        });


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(ProfilePage.this);
                isInternetPresent = cd.isConnectingToInternet();

                if (isInternetPresent) {

                    final PkDialog mDialog = new PkDialog(ProfilePage.this);
                    mDialog.setDialogTitle(getResources().getString(R.string.profile_lable_logout_title));
                    mDialog.setDialogMessage(getResources().getString(R.string.profile_lable_logout_message));
                    mDialog.setPositiveButton(getResources().getString(R.string.profile_lable_logout_yes), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                            postRequest_Logout(Iconstant.logout_url);
                        }
                    });
                    mDialog.setNegativeButton(getResources().getString(R.string.profile_lable_logout_no), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                        }
                    });
                    mDialog.show();

                } else {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });

        layout_changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilePage.this, ChangePassword.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        Et_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;

                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    cd = new ConnectionDetector(ProfilePage.this);
                    isInternetPresent = cd.isConnectingToInternet();
                    CloseKeyboard(Et_name);

                    if (Et_name.getText().toString().length() == 0) {
                        Alert(getResources().getString(R.string.action_error), getResources().getString(R.string.profile_lable_error_name));
                    } else {
                        if (isInternetPresent) {
                            postRequest_editUserName(Iconstant.profile_edit_userName_url);
                        } else {
                            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                        }
                    }
                    handled = true;
                }
                return handled;
            }
        });

       /* Et_country_code.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT ) {
                    Et_mobileno.requestFocus();
                    handled = true;
                }
                return handled;
            }
        });*/


        Rl_country_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
            }
        });

        picker.setListener(new CountryPickerListener() {
            @Override
            public void onSelectCountry(String name, String code, String dialCode) {
                picker.dismiss();
                Tv_countryCode.setText(dialCode.replace("+", ""));
                Et_mobileno.requestFocus();
            }
        });

        Et_mobileno.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    cd = new ConnectionDetector(ProfilePage.this);
                    isInternetPresent = cd.isConnectingToInternet();
                    CloseKeyboard(Et_name);

                    if (!isValidPhoneNumber(Et_mobileno.getText().toString())) {
                        Alert(getResources().getString(R.string.action_error), getResources().getString(R.string.profile_lable_error_mobile));
                    } else if (Tv_countryCode.getText().toString().length() == 0) {
                        Alert(getResources().getString(R.string.action_error), getResources().getString(R.string.profile_lable_error_mobilecode));
                    } else {
                        if (isInternetPresent) {
                            postRequest_editMobileNumber(Iconstant.profile_edit_mobileNo_url);
                        } else {
                            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                        }
                    }
                    handled = true;
                }
                return handled;
            }
        });

    }

    private void initialize() {
        session = new SessionManager(ProfilePage.this);
        picker = CountryPicker.newInstance("Select Country");

        tv_email = (TextView) findViewById(R.id.myprofile_emailid_textview);
        Rl_country_code = (RelativeLayout) findViewById(R.id.myprofile_textView_country_code_layout);
        Tv_countryCode = (TextView) findViewById(R.id.myprofile_country_code_textview);
        back = (RelativeLayout) findViewById(R.id.myprofile_header_back_layout);
        Et_mobileno = (EditText) findViewById(R.id.myprofile_edit_phoneno_editText);
        Et_name = (EditText) findViewById(R.id.myprofile_username_editText);
        layout_changePassword = (RelativeLayout) findViewById(R.id.myprofile_changepassword_layout);
        logout = (Button) findViewById(R.id.myprofile_logout_button);

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        UserID = user.get(SessionManager.KEY_USERID);
        UserName = user.get(SessionManager.KEY_USERNAME);
        UserMobileno = user.get(SessionManager.KEY_PHONENO);
        UserEmail = user.get(SessionManager.KEY_EMAIL);
        UserCountyCode = user.get(SessionManager.KEY_COUNTRYCODE);

        //Et_name.setImeActionLabel("Send", KeyEvent.);

        tv_email.setText(UserEmail);
        Et_name.setText(UserName);
        Et_mobileno.setText(UserMobileno);
        Tv_countryCode.setText(UserCountyCode.replace("+", ""));

        //----Code to make EditText Cursor at End of the Text------
        Et_name.setSelection(Et_name.getText().length());
        Et_mobileno.setSelection(Et_mobileno.getText().length());
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {

        final PkDialog mDialog = new PkDialog(ProfilePage.this);
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

    // validating Phone Number
    public static final boolean isValidPhoneNumber(CharSequence target) {
        if (target == null || TextUtils.isEmpty(target) || target.length() <= 9) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }
    }

    //--------------Close KeyBoard Method-----------
    private void CloseKeyboard(EditText edittext) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(edittext.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //--------------Show Dialog Method-----------
    private void showDialog(String data) {
        dialog = new Dialog(ProfilePage.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(data);
    }

    //--------------Update Mobile Number From Profile OTP Page Method-----------
    public static void updateMobileDialog(String code, String phone) {
        Et_mobileno.setText(phone);
        Tv_countryCode.setText(code.replace("+", ""));
    }

    //-----------------------Edit UserName Request-----------------
    private void postRequest_editUserName(String Url) {
        showDialog(getResources().getString(R.string.action_updating));
        System.out.println("---------------Edit Username Url-----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);
        jsonParams.put("user_name", Et_name.getText().toString());

        mRequest = new ServiceRequest(ProfilePage.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("---------------Edit Username Response-----------------" + response);
                String Sstatus = "", Smessage = "";
                try {

                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    Smessage = object.getString("response");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();

                if (Sstatus.equalsIgnoreCase("1")) {
                    session.setUserNameUpdate(Et_name.getText().toString());
                    Alert(getResources().getString(R.string.action_success), getResources().getString(R.string.profile_lable_username_success));
                } else {
                    Alert(getResources().getString(R.string.action_error), Smessage);
                }
            }

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });
    }


    //-----------------------Edit MobileNumber Request-----------------
    private void postRequest_editMobileNumber(String Url) {
        showDialog(getResources().getString(R.string.action_updating));
        System.out.println("---------------Edit MobileNumber Url-----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);
        jsonParams.put("country_code", "+" + Tv_countryCode.getText().toString());
        jsonParams.put("phone_number", Et_mobileno.getText().toString());
        jsonParams.put("otp", "");

        mRequest = new ServiceRequest(ProfilePage.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("---------------Edit MobileNumber Response-----------------" + response);
                String Sstatus = "", Smessage = "", Sotp = "", Sotp_status = "", Scountry_code = "", Sphone_number = "";
                try {

                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    Smessage = object.getString("response");
                    if (Sstatus.equalsIgnoreCase("1")) {
                        Sotp = object.getString("otp");
                        Sotp_status = object.getString("otp_status");
                        Scountry_code = object.getString("country_code");
                        Sphone_number = object.getString("phone_number");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();
                if (Sstatus.equalsIgnoreCase("1")) {
                    Intent intent = new Intent(ProfilePage.this, ProfileOtpPage.class);
                    intent.putExtra("Otp", Sotp);
                    intent.putExtra("Otp_Status", Sotp_status);
                    intent.putExtra("CountryCode", Scountry_code);
                    intent.putExtra("Phone", Sphone_number);
                    intent.putExtra("UserID", UserID);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else {
                    Alert(getResources().getString(R.string.action_error), Smessage);
                }
            }

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });
    }


    //-----------------------Logout Request-----------------
    private void postRequest_Logout(String Url) {
        showDialog(getResources().getString(R.string.action_logging_out));
        System.out.println("---------------LogOut Url-----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);
        jsonParams.put("device", "ANDROID");

        mRequest = new ServiceRequest(ProfilePage.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("---------------LogOut Response-----------------" + response);
                String Sstatus = "", Sresponse = "";
                try {

                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    Sresponse = object.getString("response");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();
                if (Sstatus.equalsIgnoreCase("1")) {
                    session.logoutUser();
                    Intent local = new Intent();
                    local.setAction("com.app.logout");
                    ProfilePage.this.sendBroadcast(local);

                    onBackPressed();
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                } else {
                    Alert(getResources().getString(R.string.action_error), Sresponse);
                }
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
}

