package com.cabily.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cabily.HockeyApp.ActivityHockeyApp;
import com.cabily.iconstant.Iconstant;
import com.cabily.pojo.CancelTripPojo;
import com.cabily.pojo.MyRideDetailPojo;
import com.cabily.pojo.PaymentListPojo;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mylibrary.volley.AppController;
import com.mylibrary.volley.VolleyErrorResponse;
import com.mylibrary.xmpp.ChatService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Prem Kumar and Anitha on 10/29/2015.
 */
public class MyRidesDetail extends ActivityHockeyApp {
    private RelativeLayout back;
    private static Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private SessionManager session;
    private String UserID = "";

    private StringRequest postrequest;
    Dialog dialog;
    ArrayList<MyRideDetailPojo> itemlist;
    ArrayList<CancelTripPojo> itemlist_reason;

    private TextView Tv_carType, Tv_carNo, Tv_rideStatus, Tv_address, Tv_pickup, Tv_drop,
            Tv_rideDistance, Tv_timeTaken, Tv_waitTime, Tv_totalBill, Tv_totalPaid, Tv_couponDiscount, Tv_walletUsuage;
    private RelativeLayout Rl_favorite, Rl_priceBottom, Rl_button;
    private RelativeLayout Rl_address, Rl_pickup;
    private ImageView Iv_favorite;
    private LinearLayout Ll_cancelTrip, Ll_payment, Ll_mailInvoice, Ll_reportIssue, Ll_trackRide;

    private GoogleMap googleMap;
    private String SrideId_intent = "";
    private boolean isPickUpAvailable = false;
    private boolean isSummaryAvailable = false;
    private boolean isReasonAvailable = false;
    private boolean isFareAvailable = false;
    private boolean isTrackRideAvailable = false;

    public static MyRidesDetail myrideDetail_class;

    private String Str_LocationLatitude = "", Str_LocationLongitude = "";

    //------Invoice Dialog Declaration-----
    private EditText Et_dialog_InvoiceEmail;
    private MaterialDialog invoice_dialog;

    //------Favourite Dialog Declaration-----
    private EditText Et_dialog_FavouriteTitle;
    private MaterialDialog favourite_dialog;

    //------Tip Declaration-----
    private EditText Et_tip_Amount;
    private Button Bt_tip_Apply;
    private RelativeLayout Rl_tip;

    public class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.package.ACTION_CLASS_REFRESH")) {
                if (isInternetPresent) {
                    postRequest_MyRides(Iconstant.myride_details_url);
                }
            }
        }
    }

    private RefreshReceiver refreshReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myrides_detail);
        myrideDetail_class = MyRidesDetail.this;
        initialize();
        initializeMap();

        //Start XMPP Chat Service
        ChatService.startUserAction(MyRidesDetail.this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.enter, R.anim.exit);
                finish();
            }
        });

        Ll_cancelTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = View.inflate(MyRidesDetail.this, R.layout.material_alert_dialog, null);
                final MaterialDialog mdialog = new MaterialDialog(MyRidesDetail.this);
                mdialog.setContentView(view)
                      .setPositiveButton(
                                getResources().getString(R.string.my_rides_detail_cancel_ride_alert_yes), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mdialog.dismiss();

                                        cd = new ConnectionDetector(MyRidesDetail.this);
                                        isInternetPresent = cd.isConnectingToInternet();

                                        if (isInternetPresent) {
                                            postRequest_CancelRides_Reason(Iconstant.cancel_myride_reason_url);
                                        } else {
                                            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                                        }
                                    }
                                }
                        )
                      .setNegativeButton(
                              getResources().getString(R.string.my_rides_detail_cancel_ride_alert_no), new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {
                                      mdialog.dismiss();
                                  }
                              }
                      )
                      .show();

                TextView alert_title=(TextView)view.findViewById(R.id.material_alert_message_label);
                TextView alert_message=(TextView)view.findViewById(R.id.material_alert_message_textview);
                alert_title.setText(getResources().getString(R.string.my_rides_detail_cancel_ride_alert_title));
                alert_message.setText(getResources().getString(R.string.my_rides_detail_cancel_ride_alert));
            }
        });

        Ll_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(MyRidesDetail.this);
                isInternetPresent = cd.isConnectingToInternet();

                if (isInternetPresent) {
                    Intent passIntent = new Intent(MyRidesDetail.this, MyRidePaymentList.class);
                    passIntent.putExtra("RideID", SrideId_intent);
                    startActivity(passIntent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });

        Ll_mailInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mailInvoice();
            }
        });

        Ll_reportIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });

        Ll_trackRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(MyRidesDetail.this);
                isInternetPresent = cd.isConnectingToInternet();

                if (isInternetPresent) {
                    postRequest_TrackRide(Iconstant.myride_details_track_your_ride_url);
                } else {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });

        Iv_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemlist.get(0).getIsFavLocation().equalsIgnoreCase("0")) {
                    favouriteAddress();
                }
            }
        });


        Bt_tip_Apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(MyRidesDetail.this);
                isInternetPresent = cd.isConnectingToInternet();

                if(Et_tip_Amount.getText().toString().length()>0)
                {
                    if (isInternetPresent) {
                        if(Bt_tip_Apply.getText().toString().equalsIgnoreCase(getResources().getString(R.string.my_rides_detail_tip_apply_label)))
                        {
                            postRequest_Tip(Iconstant.tip_add_url,"Apply");
                        }
                        else if(Bt_tip_Apply.getText().toString().equalsIgnoreCase(getResources().getString(R.string.my_rides_detail_tip_remove_label)))
                        {
                            postRequest_Tip(Iconstant.tip_remove_url,"Remove");
                        }
                    } else {
                        Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                    }
                }
                else {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.my_rides_detail_tip_empty_label));
                }

            }
        });

    }

    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.my_rides_detail_mapview)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(MyRidesDetail.this, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            }
        }

        // Changing map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Showing / hiding your current location
        googleMap.setMyLocationEnabled(false);
        // Enable / Disable zooming controls
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        // Enable / Disable my location button
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        // Enable / Disable Compass icon
        googleMap.getUiSettings().setCompassEnabled(false);
        // Enable / Disable Rotate gesture
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        // Enable / Disable zooming functionality
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.setMyLocationEnabled(false);
        //Enable / Disable Moving Function
        googleMap.getUiSettings().setAllGesturesEnabled(false);
    }

    private void initialize() {
        session = new SessionManager(MyRidesDetail.this);
        cd = new ConnectionDetector(MyRidesDetail.this);
        isInternetPresent = cd.isConnectingToInternet();
        itemlist = new ArrayList<MyRideDetailPojo>();
        itemlist_reason = new ArrayList<CancelTripPojo>();

        back = (RelativeLayout) findViewById(R.id.my_rides_detail_header_back_layout);
        Tv_carType = (TextView) findViewById(R.id.my_rides_detail_car_type);
        Tv_carNo = (TextView) findViewById(R.id.my_rides_detail_car_number);
        Tv_rideStatus = (TextView) findViewById(R.id.my_rides_detail_ride_status);
        Tv_address = (TextView) findViewById(R.id.my_rides_detail_location_textview);
        Tv_pickup = (TextView) findViewById(R.id.my_rides_detail_pickup_time_textview);
        Tv_drop = (TextView) findViewById(R.id.my_rides_detail_drop_time_textview);
        Tv_rideDistance = (TextView) findViewById(R.id.my_rides_detail_ride_distance_textview);
        Tv_timeTaken = (TextView) findViewById(R.id.my_rides_detail_time_taken_textview);
        Tv_waitTime = (TextView) findViewById(R.id.my_rides_detail_wait_time_textview);
        Tv_totalBill = (TextView) findViewById(R.id.my_rides_detail_total_bill_textview);
        Tv_totalPaid = (TextView) findViewById(R.id.my_rides_detail_total_paid_textview);
        Tv_couponDiscount = (TextView) findViewById(R.id.my_rides_detail_coupon_discount_textview);
        Tv_walletUsuage = (TextView) findViewById(R.id.my_rides_detail_wallet_usuage_textview);

        Rl_favorite = (RelativeLayout) findViewById(R.id.my_rides_detail_favorite_layout);
        Rl_priceBottom = (RelativeLayout) findViewById(R.id.my_rides_detail_price_layout);
        Rl_button = (RelativeLayout) findViewById(R.id.my_rides_detail_button_layout);
        Iv_favorite = (ImageView) findViewById(R.id.my_rides_detail_favorite_imageView);
        Rl_address = (RelativeLayout) findViewById(R.id.my_rides_detail_address_layout);
        Rl_pickup = (RelativeLayout) findViewById(R.id.my_rides_detail_time_layout);

        Ll_cancelTrip = (LinearLayout) findViewById(R.id.my_rides_detail_cancel_trip_layout);
        Ll_payment = (LinearLayout) findViewById(R.id.my_rides_detail_payment_layout);
        Ll_mailInvoice = (LinearLayout) findViewById(R.id.my_rides_detail_mail_invoice_layout);
        Ll_reportIssue = (LinearLayout) findViewById(R.id.my_rides_detail_report_issue_layout);
        Ll_trackRide = (LinearLayout) findViewById(R.id.my_rides_detail_track_ride_layout);

        Et_tip_Amount =(EditText)findViewById(R.id.my_rides_detail_tip_editText);
        Bt_tip_Apply =(Button)findViewById(R.id.my_rides_detail_tip_apply_button);
        Rl_tip =(RelativeLayout)findViewById(R.id.my_rides_detail_tip_layout);

        // -----code to refresh drawer using broadcast receiver-----
        refreshReceiver = new RefreshReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.package.ACTION_CLASS_REFRESH");
        registerReceiver(refreshReceiver, intentFilter);

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        UserID = user.get(SessionManager.KEY_USERID);

        Intent intent = getIntent();
        SrideId_intent = intent.getStringExtra("RideID");

        if (isInternetPresent) {
            postRequest_MyRides(Iconstant.myride_details_url);
        } else {
            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
        }
    }


    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(MyRidesDetail.this);
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

    //code to Check Email Validation
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    //--------------------Code to set error for EditText-----------------------
    private void erroredit(EditText editname, String msg) {
        Animation shake = AnimationUtils.loadAnimation(MyRidesDetail.this, R.anim.shake);
        editname.startAnimation(shake);

        ForegroundColorSpan fgcspan = new ForegroundColorSpan(Color.parseColor("#CC0000"));
        SpannableStringBuilder ssbuilder = new SpannableStringBuilder(msg);
        ssbuilder.setSpan(fgcspan, 0, msg.length(), 0);
        editname.setError(ssbuilder);
    }


    //----------------------Code for TextWatcher-------------------------
    private final TextWatcher mailInvoice_EditorWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            //clear error symbol after entering text
            if (Et_dialog_InvoiceEmail.getText().length() > 0) {
                Et_dialog_InvoiceEmail.setError(null);
            }
        }
    };

    private final TextWatcher favourite_EditorWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            //clear error symbol after entering text
            if (Et_dialog_FavouriteTitle.getText().length() > 0) {
                Et_dialog_FavouriteTitle.setError(null);
            }
        }
    };


    //----------Method to Send Email--------
    protected void sendEmail() {
        String[] TO = {"info@zoplay.com"};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Message");
        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MyRidesDetail.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    //----------Method for Invoice Email--------
    private void mailInvoice() {
        View view = View.inflate(MyRidesDetail.this, R.layout.mail_invoice_dialog, null);
        invoice_dialog = new MaterialDialog(MyRidesDetail.this);
        invoice_dialog.setContentView(view)
                .setNegativeButton(getResources().getString(R.string.my_rides_detail_cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                invoice_dialog.dismiss();
                            }
                        }
                )
                .setPositiveButton(getResources().getString(R.string.my_rides_detail_send), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cd = new ConnectionDetector(MyRidesDetail.this);
                                isInternetPresent = cd.isConnectingToInternet();

                                if (!isValidEmail(Et_dialog_InvoiceEmail.getText().toString())) {
                                    erroredit(Et_dialog_InvoiceEmail, getResources().getString(R.string.register_label_alert_email));
                                } else {
                                    if (isInternetPresent) {
                                        postRequest_EmailInvoice(Iconstant.myride_details_inVoiceEmail_url, Et_dialog_InvoiceEmail.getText().toString(), SrideId_intent);
                                    } else {
                                        Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                                    }
                                }
                            }
                        }
                )
                .show();

        Et_dialog_InvoiceEmail = (EditText) view.findViewById(R.id.mail_invoice_email_edittext);
        Et_dialog_InvoiceEmail.addTextChangedListener(mailInvoice_EditorWatcher);

        Et_dialog_InvoiceEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(Et_dialog_InvoiceEmail.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });
    }


    //----------Method for Favourite Address--------
    private void favouriteAddress() {
        View view = View.inflate(MyRidesDetail.this, R.layout.myride_detail_favourite_dialog, null);
        favourite_dialog = new MaterialDialog(MyRidesDetail.this);
        favourite_dialog.setContentView(view)
                .setNegativeButton(getResources().getString(R.string.my_rides_detail_cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                favourite_dialog.dismiss();
                            }
                        }
                )
                .setPositiveButton(getResources().getString(R.string.my_rides_detail_favourite_apply), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cd = new ConnectionDetector(MyRidesDetail.this);
                                isInternetPresent = cd.isConnectingToInternet();

                                if (Et_dialog_FavouriteTitle.getText().toString().length() == 0) {
                                    erroredit(Et_dialog_FavouriteTitle, getResources().getString(R.string.my_rides_detail_favourite_alert_title));
                                } else {
                                    if (isInternetPresent) {
                                        postRequest_FavoriteSave(Iconstant.favoritelist_add_url);
                                    } else {
                                        Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                                    }
                                }
                            }
                        }
                )
                .show();

        Et_dialog_FavouriteTitle = (EditText) view.findViewById(R.id.myride_detail_favourite_title_edittext);
        Et_dialog_FavouriteTitle.addTextChangedListener(favourite_EditorWatcher);

        Et_dialog_FavouriteTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(Et_dialog_FavouriteTitle.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });

    }


    //-----------------------MyRide Detail Post Request-----------------
    private void postRequest_MyRides(String Url) {
        dialog = new Dialog(MyRidesDetail.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));

        System.out.println("-------------MyRide Detail Url----------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------MyRide Detail Response----------------" + response);

                        String Sstatus = "";
                        Currency currencycode;

                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if (Sstatus.equalsIgnoreCase("1")) {
                                JSONObject response_object = object.getJSONObject("response");
                                if (response_object.length() > 0) {
                                    JSONObject detail_object = response_object.getJSONObject("details");
                                    if (detail_object.length() > 0) {
                                        itemlist.clear();
                                        MyRideDetailPojo pojo = new MyRideDetailPojo();

                                        currencycode = Currency.getInstance(getLocale(detail_object.getString("currency")));
                                        pojo.setCurrrencySymbol(currencycode.getSymbol());
                                        pojo.setCarType(detail_object.getString("cab_type"));
                                        pojo.setRideId(detail_object.getString("ride_id"));
                                        pojo.setRideStatus(detail_object.getString("ride_status"));
                                        pojo.setDisplayStatus(detail_object.getString("disp_status"));
                                        pojo.setDoCancelAction(detail_object.getString("do_cancel_action"));
                                        pojo.setDoTrackAction(detail_object.getString("do_track_action"));
                                        pojo.setIsFavLocation(detail_object.getString("is_fav_location"));
                                        pojo.setPay_status(detail_object.getString("pay_status"));
                                        pojo.setPickup(getResources().getString(R.string.my_rides_detail_pickup_textview) + " " + detail_object.getString("pickup_date"));

                                        JSONObject pickup_object = detail_object.getJSONObject("pickup");
                                        if (pickup_object.length() > 0) {
                                            pojo.setAddress(pickup_object.getString("location"));
                                            JSONObject latlong_object = pickup_object.getJSONObject("latlong");
                                            if (latlong_object.length() > 0) {
                                                pojo.setLocationLat(latlong_object.getString("lat"));
                                                pojo.setLocationLong(latlong_object.getString("lon"));
                                            }

                                            isPickUpAvailable = true;
                                        } else {
                                            isPickUpAvailable = false;
                                        }

                                        if (detail_object.getString("ride_status").equalsIgnoreCase("Completed") || detail_object.getString("ride_status").equalsIgnoreCase("Finished")) {
                                            pojo.setDrop(getResources().getString(R.string.my_rides_detail_drop_textview) + " " + " " + detail_object.getString("drop_date"));

                                            JSONObject summary_object = detail_object.getJSONObject("summary");
                                            if (summary_object.length() > 0) {
                                                pojo.setRideDistance(summary_object.getString("ride_distance"));
                                                pojo.setTimeTaken(summary_object.getString("ride_duration"));
                                                pojo.setWaitTime(summary_object.getString("waiting_duration"));

                                                isSummaryAvailable = true;
                                            } else {
                                                isSummaryAvailable = false;
                                            }

                                            JSONObject fare_object = detail_object.getJSONObject("fare");
                                            if (fare_object.length() > 0) {
                                                pojo.setTotalBill(fare_object.getString("total_bill"));
                                                pojo.setTotalPaid(fare_object.getString("total_paid"));
                                                pojo.setCouponDiscount(fare_object.getString("coupon_discount"));
                                                pojo.setWalletUsuage(fare_object.getString("wallet_usage"));
                                                pojo.setTip_amount(fare_object.getString("tips_amount"));

                                                isFareAvailable = true;
                                            } else {
                                                isFareAvailable = false;
                                            }
                                        }

                                        itemlist.add(pojo);
                                    }
                                }
                            }




                            //------------OnPost Execute------------
                            if (Sstatus.equalsIgnoreCase("1")) {
                                if (itemlist.size() > 0) {
                                    Rl_address.setVisibility(View.VISIBLE);
                                    Rl_pickup.setVisibility(View.VISIBLE);

                                    Tv_carType.setText(getResources().getString(R.string.my_rides_detail_city_taxi_textview) + " " + itemlist.get(0).getCarType() + " " + getResources().getString(R.string.my_rides_detail_ride_textview));
                                    Tv_carNo.setText(getResources().getString(R.string.my_rides_detail_crn_textview) + " " + itemlist.get(0).getRideId());
                                    Tv_rideStatus.setText(itemlist.get(0).getRideStatus());

                                    if (isPickUpAvailable) {
                                        Tv_address.setText(itemlist.get(0).getAddress());
                                        Tv_pickup.setText(itemlist.get(0).getPickup());


                                        //set marker for User location.
                                        if (itemlist.get(0).getLocationLat() != null && itemlist.get(0).getLocationLong() != null) {

                                            Str_LocationLatitude = itemlist.get(0).getLocationLat();
                                            Str_LocationLongitude = itemlist.get(0).getLocationLong();

                                            googleMap.addMarker(new MarkerOptions()
                                                    .position(new LatLng(Double.parseDouble(itemlist.get(0).getLocationLat()), Double.parseDouble(itemlist.get(0).getLocationLong())))
                                                            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker_icon2)));
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                                            // Move the camera to last position with a zoom level
                                            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Double.parseDouble(itemlist.get(0).getLocationLat()), Double.parseDouble(itemlist.get(0).getLocationLong()))).zoom(15).build();
                                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                        }
                                    }


                                    if (itemlist.get(0).getRideStatus().equalsIgnoreCase("Completed") || itemlist.get(0).getRideStatus().equalsIgnoreCase("Finished")) {
                                        Tv_drop.setVisibility(View.VISIBLE);
                                        Tv_drop.setText(itemlist.get(0).getDrop());
                                        if (isSummaryAvailable) {
                                            Tv_rideDistance.setText(itemlist.get(0).getRideDistance() + " " + getResources().getString(R.string.my_rides_detail_kms_textview));
                                            Tv_timeTaken.setText(itemlist.get(0).getTimeTaken() + " " + getResources().getString(R.string.my_rides_detail_mins_textview));
                                            Tv_waitTime.setText(itemlist.get(0).getWaitTime() + " " + getResources().getString(R.string.my_rides_detail_mins_textview));
                                        }

                                        if (isFareAvailable) {
                                            Tv_totalBill.setText(itemlist.get(0).getCurrrencySymbol() + itemlist.get(0).getTotalBill());
                                            Tv_totalPaid.setText(itemlist.get(0).getCurrrencySymbol() + itemlist.get(0).getTotalPaid());
                                            Tv_walletUsuage.setText(itemlist.get(0).getCurrrencySymbol() + itemlist.get(0).getWalletUsuage());

                                            if (itemlist.get(0).getCouponDiscount().equalsIgnoreCase("0")) {
                                                Tv_couponDiscount.setVisibility(View.GONE);
                                            } else {
                                                Tv_couponDiscount.setVisibility(View.VISIBLE);
                                                Tv_couponDiscount.setText(getResources().getString(R.string.my_rides_detail_coupon_discount_textview) + " " + itemlist.get(0).getCurrrencySymbol() + itemlist.get(0).getCouponDiscount());
                                            }

                                            if (itemlist.get(0).getWalletUsuage().equalsIgnoreCase("0")) {
                                                Tv_walletUsuage.setVisibility(View.GONE);
                                            } else {
                                                Tv_walletUsuage.setVisibility(View.VISIBLE);
                                                Tv_walletUsuage.setText(getResources().getString(R.string.my_rides_detail_wallet_usuage_textview) + " " + itemlist.get(0).getCurrrencySymbol() + itemlist.get(0).getWalletUsuage());
                                            }
                                        }

                                        Rl_priceBottom.setVisibility(View.VISIBLE);
                                    } else {
                                        Rl_priceBottom.setVisibility(View.GONE);
                                        Tv_drop.setVisibility(View.GONE);
                                    }


                                    //------------Button Change Function-------
                                    if (itemlist.get(0).getPay_status().equalsIgnoreCase("Pending") || itemlist.get(0).getPay_status().equalsIgnoreCase("Processing")) {
                                        Ll_cancelTrip.setVisibility(View.GONE);
                                        Ll_payment.setVisibility(View.VISIBLE);
                                        Ll_mailInvoice.setVisibility(View.GONE);
                                        Ll_reportIssue.setVisibility(View.GONE);
                                        Rl_favorite.setVisibility(View.GONE);
                                        Rl_tip.setVisibility(View.VISIBLE);
                                    }

                                    if (itemlist.get(0).getDoCancelAction().equalsIgnoreCase("1")) {
                                        Ll_cancelTrip.setVisibility(View.VISIBLE);
                                        Ll_payment.setVisibility(View.GONE);
                                        Ll_mailInvoice.setVisibility(View.GONE);
                                        Ll_reportIssue.setVisibility(View.GONE);
                                        Rl_favorite.setVisibility(View.GONE);
                                        Rl_tip.setVisibility(View.GONE);
                                    }

                                    if (itemlist.get(0).getRideStatus().equalsIgnoreCase("Completed")) {
                                        Ll_cancelTrip.setVisibility(View.GONE);
                                        Ll_payment.setVisibility(View.GONE);
                                        Ll_mailInvoice.setVisibility(View.VISIBLE);
                                        Ll_reportIssue.setVisibility(View.VISIBLE);
                                        Rl_favorite.setVisibility(View.VISIBLE);
                                        Rl_tip.setVisibility(View.GONE);

                                        //Programmatically making textView to left of layout
                                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                                        params.addRule(RelativeLayout.LEFT_OF, R.id.my_rides_detail_favorite_layout);
                                        params.addRule(RelativeLayout.CENTER_VERTICAL, R.id.my_rides_detail_favorite_layout);
                                        Tv_rideStatus.setLayoutParams(params);
                                    }

                                    //------Show and Hide the Button Layout------
                                    if (itemlist.get(0).getPay_status().equalsIgnoreCase("Pending") || itemlist.get(0).getPay_status().equalsIgnoreCase("Processing") || itemlist.get(0).getDoCancelAction().equalsIgnoreCase("1") || itemlist.get(0).getRideStatus().equalsIgnoreCase("Completed")) {
                                        Rl_button.setVisibility(View.VISIBLE);
                                    } else {
                                        Rl_button.setVisibility(View.GONE);
                                    }

                                    //---------Changing Favourite Color-----
                                    if (itemlist.get(0).getIsFavLocation().equalsIgnoreCase("1")) {
                                        Iv_favorite.setImageResource(R.drawable.heart_red_icon);
                                        Iv_favorite.setEnabled(false);
                                    } else {
                                        Iv_favorite.setImageResource(R.drawable.heart_grey_icon);
                                        Iv_favorite.setEnabled(true);
                                    }

                                    //------Show and Hide Track Ride Button Layout------
                                    if (itemlist.get(0).getDoTrackAction().equalsIgnoreCase("1")) {
                                        Ll_trackRide.setVisibility(View.VISIBLE);
                                    } else {
                                        Ll_trackRide.setVisibility(View.GONE);
                                    }

                                    //------Show and Hide Tip Layout------
                                    if(itemlist.get(0).getTip_amount().equalsIgnoreCase("0"))
                                    {
                                        Bt_tip_Apply.setText(getResources().getString(R.string.my_rides_detail_tip_apply_label));
                                        Bt_tip_Apply.setBackgroundColor(0xFF01A7CD);
                                        Et_tip_Amount.setFocusable(true);
                                        Et_tip_Amount.setText("");
                                    }
                                    else
                                    {
                                        Bt_tip_Apply.setText(getResources().getString(R.string.my_rides_detail_tip_remove_label));
                                        Bt_tip_Apply.setBackgroundColor(0xFFCC0000);
                                        Et_tip_Amount.setFocusable(false);
                                        Et_tip_Amount.setText(itemlist.get(0).getTip_amount());
                                    }

                                }
                            } else {
                                String Sresponse = object.getString("response");
                                Alert(getResources().getString(R.string.alert_label_title), Sresponse);
                            }

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(MyRidesDetail.this, error);
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
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_id", UserID);
                jsonParams.put("ride_id", SrideId_intent);
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }


    //-----------------------MyRide Cancel Reason Post Request-----------------
    private void postRequest_CancelRides_Reason(String Url) {
        dialog = new Dialog(MyRidesDetail.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_pleasewait));


        System.out.println("-------------MyRide Cancel Reason Url----------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------MyRide Cancel Reason Response----------------" + response);

                        String Sstatus = "";

                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if (Sstatus.equalsIgnoreCase("1")) {
                                JSONObject response_object = object.getJSONObject("response");
                                if (response_object.length() > 0) {
                                    JSONArray reason_array = response_object.getJSONArray("reason");
                                    if (reason_array.length() > 0) {
                                        itemlist_reason.clear();
                                        for (int i = 0; i < reason_array.length(); i++) {
                                            JSONObject reason_object = reason_array.getJSONObject(i);
                                            CancelTripPojo pojo = new CancelTripPojo();
                                            pojo.setReason(reason_object.getString("reason"));
                                            pojo.setReasonId(reason_object.getString("id"));

                                            itemlist_reason.add(pojo);
                                        }

                                        isReasonAvailable = true;
                                    } else {
                                        isReasonAvailable = false;
                                    }
                                }
                            } else {
                                String Sresponse = object.getString("response");
                                Alert(getResources().getString(R.string.alert_label_title), Sresponse);
                            }


                            if (Sstatus.equalsIgnoreCase("1") && isReasonAvailable) {
                                Intent passIntent = new Intent(MyRidesDetail.this, MyRideCancelTrip.class);
                                Bundle bundleObject = new Bundle();
                                bundleObject.putSerializable("Reason", itemlist_reason);
                                passIntent.putExtras(bundleObject);
                                passIntent.putExtra("RideID", SrideId_intent);
                                startActivity(passIntent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            }

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(MyRidesDetail.this, error);
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
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_id", UserID);
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }


    //-----------------------MyRide Email Invoice Post Request-----------------
    private void postRequest_EmailInvoice(String Url, final String Semail, final String SrideId) {
        dialog = new Dialog(MyRidesDetail.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_sending_invoice));

        System.out.println("-------------MyRide Email Invoice Url----------------" + Url);
        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        String Sstatus = "", Sresponse = "";
                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            Sresponse = object.getString("response");
                            if (Sstatus.equalsIgnoreCase("1")) {
                                invoice_dialog.dismiss();
                                Alert(getResources().getString(R.string.action_success), Sresponse);
                            } else {
                                Sresponse = object.getString("response");
                                Alert(getResources().getString(R.string.alert_label_title), Sresponse);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(MyRidesDetail.this, error);
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
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("email", Semail);
                jsonParams.put("ride_id", SrideId);
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }


    //-----------------------Favourite Save Post Request-----------------
    private void postRequest_FavoriteSave(String Url) {
        dialog = new Dialog(MyRidesDetail.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_saving));


        System.out.println("-------------Favourite Save Url----------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------Favourite Save Response----------------" + response);
                        String Sstatus = "", Smessage = "";

                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            Smessage = object.getString("message");

                            // close keyboard
                            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            mgr.hideSoftInputFromWindow(Et_dialog_FavouriteTitle.getWindowToken(), 0);

                            if (Sstatus.equalsIgnoreCase("1")) {
                                final MaterialDialog mdialog = new MaterialDialog(MyRidesDetail.this);
                                mdialog.setTitle(getResources().getString(R.string.action_success))
                                        .setMessage(Smessage)
                                        .setPositiveButton(
                                                "OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        mdialog.dismiss();
                                                        favourite_dialog.dismiss();
                                                        Iv_favorite.setImageResource(R.drawable.heart_red_icon);
                                                        itemlist.get(0).setIsFavLocation("1");
                                                    }
                                                }
                                        )
                                        .show();
                            } else {
                                Alert(getResources().getString(R.string.alert_label_title), Smessage);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        dialog.dismiss();

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(MyRidesDetail.this, error);
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
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_id", UserID);
                jsonParams.put("title", Et_dialog_FavouriteTitle.getText().toString());
                jsonParams.put("latitude", Str_LocationLatitude);
                jsonParams.put("longitude", Str_LocationLongitude);
                jsonParams.put("address", Tv_address.getText().toString());
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }


    //-----------------------Track Ride Post Request-----------------
    private void postRequest_TrackRide(String Url) {
        dialog = new Dialog(MyRidesDetail.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_pleasewait));


        System.out.println("-------------Track Ride Url----------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------Track Ride Response----------------" + response);
                        String Sstatus = "";
                        String driverID = "", driverName = "", driverImage = "", driverRating = "",
                                driverLat = "", driverLong = "", driverTime = "", rideID = "", driverMobile = "",
                                driverCar_no = "", driverCar_model = "", userLat = "", userLong = "";
                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if (Sstatus.equalsIgnoreCase("1")) {
                                JSONObject response_object = object.getJSONObject("response");
                                if (response_object.length() > 0) {
                                    JSONObject driver_profile_object = response_object.getJSONObject("driver_profile");
                                    if (driver_profile_object.length() > 0) {
                                        driverID = driver_profile_object.getString("driver_id");
                                        driverName = driver_profile_object.getString("driver_name");
                                        driverImage = driver_profile_object.getString("driver_image");
                                        driverRating = driver_profile_object.getString("driver_review");
                                        driverLat = driver_profile_object.getString("driver_lat");
                                        driverLong = driver_profile_object.getString("driver_lon");
                                        driverTime = driver_profile_object.getString("min_pickup_duration");
                                        rideID = driver_profile_object.getString("ride_id");
                                        driverMobile = driver_profile_object.getString("phone_number");
                                        driverCar_no = driver_profile_object.getString("vehicle_number");
                                        driverCar_model = driver_profile_object.getString("vehicle_model");
                                        userLat = driver_profile_object.getString("rider_lat");
                                        userLong = driver_profile_object.getString("rider_lon");

                                        isTrackRideAvailable = true;
                                    }
                                }
                            } else {
                                String Sresponse = object.getString("response");
                                Alert(getResources().getString(R.string.alert_label_title), Sresponse);
                            }


                            if (Sstatus.equalsIgnoreCase("1") && isTrackRideAvailable) {
                                Intent i = new Intent(MyRidesDetail.this, MyRideDetailTrackRide.class);
                                i.putExtra("driverID", driverID);
                                i.putExtra("driverName", driverName);
                                i.putExtra("driverImage", driverImage);
                                i.putExtra("driverRating", driverRating);
                                i.putExtra("driverLat", driverLat);
                                i.putExtra("driverLong", driverLong);
                                i.putExtra("driverTime", driverTime);
                                i.putExtra("rideID", rideID);
                                i.putExtra("driverMobile", driverMobile);
                                i.putExtra("driverCar_no", driverCar_no);
                                i.putExtra("driverCar_model", driverCar_model);
                                i.putExtra("userLat", userLat);
                                i.putExtra("userLong", userLong);
                                startActivity(i);
                                overridePendingTransition(R.anim.enter, R.anim.exit);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(MyRidesDetail.this, error);
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
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("rideId", SrideId_intent);
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }



    //-----------------------Tip Post Request-----------------
    private void postRequest_Tip(String Url, final String tipStatus) {
        dialog = new Dialog(MyRidesDetail.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_pleasewait));


        System.out.println("-------------tip Url----------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------tip Response----------------" + response);
                        String Sstatus = "", Sresponse = "";
                        try {

                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if (Sstatus.equalsIgnoreCase("1")) {

                                JSONObject response_Object=object.getJSONObject("response");
                                Sresponse = response_Object.getString("msg");

                                if(tipStatus.equalsIgnoreCase("Apply"))
                                {
                                    Bt_tip_Apply.setText(getResources().getString(R.string.my_rides_detail_tip_remove_label));
                                    Bt_tip_Apply.setBackgroundColor(0xFFCC0000);
                                    Et_tip_Amount.setEnabled(false);
                                }
                                else
                                {
                                    Bt_tip_Apply.setText(getResources().getString(R.string.my_rides_detail_tip_apply_label));
                                    Bt_tip_Apply.setBackgroundColor(0xFF01A7CD);
                                    Et_tip_Amount.setEnabled(true);
                                    Et_tip_Amount.setText("");
                                }
                                Alert(getResources().getString(R.string.action_success), Sresponse);
                            } else {
                                Alert(getResources().getString(R.string.alert_label_title), Sresponse);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(MyRidesDetail.this, error);
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
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("ride_id", SrideId_intent);

                if(tipStatus.equalsIgnoreCase("Apply"))
                {
                    jsonParams.put("tips_amount", Et_tip_Amount.getText().toString());
                }
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }



    //-----------------Move Back on pressed phone back button------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {
            onBackPressed();
            finish();
            overridePendingTransition(R.anim.enter, R.anim.exit);
            return true;
        }
        return false;
    }


    @Override
    public void onDestroy() {
        // Unregister the logout receiver
        unregisterReceiver(refreshReceiver);
        super.onDestroy();
    }
}
