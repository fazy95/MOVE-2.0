package com.cabily.utils;

import java.util.HashMap;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.cabily.app.SingUpAndSignIn;
import com.cabily.app.SplashPage;

public class SessionManager {
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "PremKumar";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_EMAIL = "email";
    public static final String KEY_USERID = "userid";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_USERIMAGE = "userimage";

    public static final String KEY_COUNTRYCODE = "countrycode";
    public static final String KEY_PHONENO = "phoneno";
    public static final String KEY_REFERAL_CODE = "referalcode";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_CATEGORY_ID = "categoryId";

    public static final String KEY_COUPON_CODE = "coupon";
    public static final String KEY_WALLET_AMOUNT = "walletAmount";

    public static final String KEY_XMPP_USERID = "xmppUserId";
    public static final String KEY_XMPP_SEC_KEY = "xmppSecKey";



    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(String email, String userid, String username, String userimage, String countrycode, String phoneno, String referalcode, String category) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_USERID, userid);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USERIMAGE, userimage);
        editor.putString(KEY_COUNTRYCODE, countrycode);
        editor.putString(KEY_PHONENO, phoneno);
        editor.putString(KEY_REFERAL_CODE, referalcode);
        editor.putString(KEY_CATEGORY, category);

        // commit changes
        editor.commit();
    }

    public void setCategoryID(String categoryID)
    {
        editor.putString(KEY_CATEGORY_ID, categoryID);
        editor.commit();
    }


    public void createWalletAmount(String amount)
    {
        editor.putString(KEY_WALLET_AMOUNT, amount);
        // commit changes
        editor.commit();
    }

    //------username update code-----
    public void setUserNameUpdate(String name) {
        editor.putString(KEY_USERNAME, name);
        editor.commit();
    }

    //------MobileNumber update code-----
    public void setMobileNumberUpdate(String code,String mobile) {
        editor.putString(KEY_COUNTRYCODE, code);
        editor.putString(KEY_PHONENO, mobile);
        editor.commit();
    }

    //------string user coupon code-----
    public void setCouponCode(String code) {
        editor.putString(KEY_COUPON_CODE, code);
        editor.commit();
    }

    //------ Xmpp Connect Secrect Code-----
    public void setXmppKey(String userId,String secretKey) {
        editor.putString(KEY_XMPP_USERID, userId);
        editor.putString(KEY_XMPP_SEC_KEY, secretKey);
        editor.commit();
    }


    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, ""));
        user.put(KEY_USERID, pref.getString(KEY_USERID, ""));
        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, ""));
        user.put(KEY_USERIMAGE, pref.getString(KEY_USERIMAGE, ""));
        user.put(KEY_COUNTRYCODE, pref.getString(KEY_COUNTRYCODE, ""));
        user.put(KEY_PHONENO, pref.getString(KEY_PHONENO, ""));
        user.put(KEY_REFERAL_CODE, pref.getString(KEY_REFERAL_CODE, ""));
        user.put(KEY_CATEGORY, pref.getString(KEY_CATEGORY, ""));

        return user;
    }


    //-----------Get user coupon code-----
    public HashMap<String, String> getCouponCode() {
        HashMap<String, String> code = new HashMap<String, String>();
        code.put(KEY_COUPON_CODE, pref.getString(KEY_COUPON_CODE, ""));

        return code;
    }


    //-----------Get CategoryId code-----
    public HashMap<String, String> getCategoryID() {
        HashMap<String, String> catID = new HashMap<String, String>();
        catID.put(KEY_CATEGORY_ID, pref.getString(KEY_CATEGORY_ID, ""));
        return catID;
    }


    //-----------Get XMPP Secret Key-----
    public HashMap<String, String> getXmppKey() {
        HashMap<String, String> code = new HashMap<String, String>();
        code.put(KEY_XMPP_USERID, pref.getString(KEY_XMPP_USERID, ""));
        code.put(KEY_XMPP_SEC_KEY, pref.getString(KEY_XMPP_SEC_KEY, ""));
        return code;
    }


    //-----------Get Wallet Amount-----
    public HashMap<String, String> getWalletAmount() {
        HashMap<String, String> code = new HashMap<String, String>();
        code.put(KEY_WALLET_AMOUNT, pref.getString(KEY_WALLET_AMOUNT, ""));
        return code;
    }

    /**
     * Clear session details
     */
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, SingUpAndSignIn.class);

        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);

    }

    /**
     * Quick check for login
     **/
    // Get Login State
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

}
