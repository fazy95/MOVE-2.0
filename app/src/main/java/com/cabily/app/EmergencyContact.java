package com.cabily.app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.cabily.HockeyApp.ActivityHockeyApp;
import com.cabily.iconstant.Iconstant;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.mylibrary.dialog.PkDialog;
import com.mylibrary.volley.ServiceRequest;
import com.mylibrary.xmpp.ChatService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


/**
 * Created by Prem Kumar and Anitha on 10/12/2015.
 */
public class EmergencyContact extends ActivityHockeyApp {
    private RelativeLayout back;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private SessionManager session;
    private EditText Et_name, Et_code, Et_phoneNo, Et_emailId;
    private RelativeLayout Rl_save;
    private RelativeLayout Rl_deleteContact;
    private ServiceRequest mRequest;
    Dialog dialog;
    private String UserID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergency_contact);
        initialize();

        //Start XMPP Chat Service
        ChatService.startUserAction(EmergencyContact.this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(back.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                onBackPressed();
                finish();
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });

        Rl_deleteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(EmergencyContact.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    deleteContact_Request(Iconstant.emergencycontact_delete_url);
                } else {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });

        Rl_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Et_name.getText().toString().length() == 0) {
                    Alert(getResources().getString(R.string.action_error), getResources().getString(R.string.emergencycontact_lable_namevalidate_textview));
                } else if (Et_code.getText().toString().length() == 0) {
                    Alert(getResources().getString(R.string.action_error), getResources().getString(R.string.emergencycontact_lable_code_validate_textview));
                } else if (!isValidPhoneNumber(Et_phoneNo.getText().toString())) {
                    Alert(getResources().getString(R.string.action_error), getResources().getString(R.string.emergencycontact_lable_mobilenovalidate_textview));
                } else if (!isValidEmail(Et_emailId.getText().toString())) {
                    Alert(getResources().getString(R.string.action_error), getResources().getString(R.string.emergencycontact_lable_email_validate_textview));
                } else {
                    cd = new ConnectionDetector(EmergencyContact.this);
                    isInternetPresent = cd.isConnectingToInternet();
                    if (isInternetPresent) {
                        updateContact_Request(Iconstant.emergencycontact_add_url);
                    } else {
                        Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                    }
                }
            }
        });


        Et_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(Et_name);
                }
                return false;
            }
        });

        Et_code.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(Et_code);
                }
                return false;
            }
        });

        Et_phoneNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(Et_phoneNo);
                }
                return false;
            }
        });

        Et_emailId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(Et_emailId);
                }
                return false;
            }
        });
    }

    private void initialize() {
        session = new SessionManager(EmergencyContact.this);
        cd = new ConnectionDetector(EmergencyContact.this);
        isInternetPresent = cd.isConnectingToInternet();

        back = (RelativeLayout) findViewById(R.id.emergency_contact_header_back_layout);
        Et_name = (EditText) findViewById(R.id.emergency_contact_name_editText);
        Et_code = (EditText) findViewById(R.id.emergency_contact_country_code_edittext);
        Et_phoneNo = (EditText) findViewById(R.id.emergency_contact_mobile_edittext);
        Et_emailId = (EditText) findViewById(R.id.emergency_contact_email_editText);
        Rl_save = (RelativeLayout) findViewById(R.id.emergency_contact_save_layout);
        Rl_deleteContact = (RelativeLayout) findViewById(R.id.emergency_contact_delete_contact_layout);

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        UserID = user.get(SessionManager.KEY_USERID);

        if (isInternetPresent) {
            displayContact_Request(Iconstant.emergencycontact_view_url);
        } else {
            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
        }
    }

    //--------------Close KeyBoard Method-----------
    private void CloseKeyboard(EditText edittext) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(edittext.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {

        final PkDialog mDialog = new PkDialog(EmergencyContact.this);
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

    //---------code to Check Email Validation------
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }


    //-----------------------Display Emergency Contact Post Request-----------------
    private void displayContact_Request(String Url) {
        dialog = new Dialog(EmergencyContact.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_pleasewait));

        System.out.println("-------------displayContact_Request Url----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);

        mRequest = new ServiceRequest(EmergencyContact.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("-------------displayContact_Request Response----------------" + response);

                String Sstatus = "", Smessage = "", Sname = "", Smobilnumber = "", Semail = "", Scountry_code = "";
                try {

                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    Smessage = object.getString("response");

                    JSONObject jobject = object.getJSONObject("emergency_contact");
                    Sname = jobject.getString("name");
                    Smobilnumber = jobject.getString("mobile");
                    Semail = jobject.getString("email");
                    Scountry_code = jobject.getString("code");

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (Sstatus.equalsIgnoreCase("1")) {
                    Et_name.setText(Sname);
                    Et_emailId.setText(Semail);
                    Et_code.setText(Scountry_code);
                    Et_phoneNo.setText(Smobilnumber);

                    if (Sname.length() == 0) {
                        Rl_deleteContact.setVisibility(View.INVISIBLE);
                    } else {
                        Rl_deleteContact.setVisibility(View.VISIBLE);
                    }
                } else {
                    Rl_deleteContact.setVisibility(View.INVISIBLE);
                    //Alert(getResources().getString(R.string.action_error), Smessage);
                }

                dialog.dismiss();
            }

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });
    }


    //-----------------------Update Emergency Contact Post Request-----------------
    private void updateContact_Request(String Url) {
        dialog = new Dialog(EmergencyContact.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_updating));

        System.out.println("-------------updateContact Url----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);
        jsonParams.put("em_name", Et_name.getText().toString());
        jsonParams.put("em_email", Et_emailId.getText().toString());
        jsonParams.put("em_mobile_code", Et_code.getText().toString());
        jsonParams.put("em_mobile", Et_phoneNo.getText().toString());

        mRequest = new ServiceRequest(EmergencyContact.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("-------------updateContact Response----------------" + response);
                String Sstatus = "", Smessage = "";
                try {

                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    Smessage = object.getString("response");

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (Sstatus.equalsIgnoreCase("1")) {
                    Rl_deleteContact.setVisibility(View.VISIBLE);
                    Alert(getResources().getString(R.string.action_success), getResources().getString(R.string.emergencycontact_lable_saved_emergencycontacts));
                } else {
                    Alert(getResources().getString(R.string.action_error), Smessage);
                }

                dialog.dismiss();
            }

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });
    }


    //-----------------------Delete Emergency Contact Post Request-----------------
    private void deleteContact_Request(String Url) {
        dialog = new Dialog(EmergencyContact.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_deleting));

        System.out.println("-------------deleteContact Url----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);

        mRequest = new ServiceRequest(EmergencyContact.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("-------------deleteContact Response----------------" + response);
                String Sstatus = "", Smessage = "";
                try {

                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    Smessage = object.getString("response");

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (Sstatus.equalsIgnoreCase("1")) {
                    Et_name.setText("");
                    Et_emailId.setText("");
                    Et_code.setText("");
                    Et_phoneNo.setText("");
                    Rl_deleteContact.setVisibility(View.INVISIBLE);

                    Alert(getResources().getString(R.string.action_success), getResources().getString(R.string.emergencycontact_lable_deletesuccess_textview));
                } else {
                    Alert(getResources().getString(R.string.action_error), Smessage);
                }

                dialog.dismiss();
            }

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });
    }


    //-----------------Move Back on pressed phone back button-------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {

            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(back.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            onBackPressed();
            finish();
            overridePendingTransition(R.anim.enter, R.anim.exit);
            return true;
        }
        return false;
    }
}
