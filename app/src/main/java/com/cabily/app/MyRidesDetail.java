package com.cabily.app;

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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.cabily.iconstant.Iconstant;
import com.cabily.pojo.CancelTripPojo;
import com.cabily.pojo.MyRideDetailPojo;
import com.cabily.subclass.ActivitySubClass;
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
import com.mylibrary.dialog.PkDialog;
import com.mylibrary.volley.ServiceRequest;
import com.mylibrary.xmpp.ChatService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

import me.drakeet.materialdialog.MaterialDialog;


/**
 * Created by Prem Kumar and Anitha on 10/29/2015.
 */
public class MyRidesDetail extends ActivitySubClass {
    private ImageView Iv_panic;
    private MaterialDialog panic_dialog;

    private RelativeLayout back;
    private static Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private SessionManager session;
    private String UserID = "";

    private ServiceRequest mRequest;
    Dialog dialog;
    ArrayList<MyRideDetailPojo> itemlist;
    ArrayList<CancelTripPojo> itemlist_reason;

    private TextView Tv_carType, Tv_carNo, Tv_rideStatus, Tv_address, Tv_pickup, Tv_drop,
            Tv_rideDistance, Tv_timeTaken, Tv_waitTime, Tv_totalBill, Tv_totalPaid, Tv_couponDiscount, Tv_walletUsuage;
    private RelativeLayout Rl_favorite, Rl_priceBottom, Rl_button;
    private RelativeLayout Rl_address, Rl_pickup;
    private ImageView Iv_favorite;
    private LinearLayout Ll_cancelTrip, Ll_payment, Ll_mailInvoice, Ll_reportIssue, Ll_trackRide,Ll_share_Ride;

    private GoogleMap googleMap;
    private String SrideId_intent = "";
    private boolean isPickUpAvailable = false;
    private boolean isSummaryAvailable = false;
    private boolean isReasonAvailable = false;
    private boolean isFareAvailable = false;
    private boolean isTrackRideAvailable = false;
    private MaterialDialog completejob_dialog;
    private EditText Et_share_trip_mobileno;
    public static MyRidesDetail myrideDetail_class;

    private String Str_LocationLatitude = "", Str_LocationLongitude = "";
    private String currencySymbol = "";

    //------Invoice Dialog Declaration-----
    private EditText Et_dialog_InvoiceEmail;
    private MaterialDialog invoice_dialog;

    //------Favourite Dialog Declaration-----
    private EditText Et_dialog_FavouriteTitle;
    private MaterialDialog favourite_dialog;

    //------Tip Declaration-----
    private EditText Et_tip_Amount;
    private Button Bt_tip_Apply;
    private RelativeLayout Rl_tip, Rl_main_tip;
    private CheckBox Cb_tip;
    private TextView Tv_tipAmount;
    private LinearLayout Ll_deleteTip;


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


                final PkDialog mDialog = new PkDialog(MyRidesDetail.this);
                mDialog.setDialogTitle(getResources().getString(R.string.my_rides_detail_cancel_ride_alert_title));
                mDialog.setDialogMessage(getResources().getString(R.string.my_rides_detail_cancel_ride_alert));
                mDialog.setPositiveButton(getResources().getString(R.string.my_rides_detail_cancel_ride_alert_yes), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                        cd = new ConnectionDetector(MyRidesDetail.this);
                        isInternetPresent = cd.isConnectingToInternet();

                        if (isInternetPresent) {
                            postRequest_CancelRides_Reason(Iconstant.cancel_myride_reason_url);
                        } else {
                            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                        }
                    }
                });
                mDialog.setNegativeButton(getResources().getString(R.string.my_rides_detail_cancel_ride_alert_no), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });
                mDialog.show();

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


        Ll_share_Ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                shareTrip();

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

                if (Et_tip_Amount.getText().toString().length() > 0) {
                    if (isInternetPresent) {
                        postRequest_Tip(Iconstant.tip_add_url, "Apply");
                    } else {
                        Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                    }
                } else {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.my_rides_detail_tip_empty_label));
                }
            }
        });

        Cb_tip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    Rl_tip.setVisibility(View.VISIBLE);
                } else {
                    Rl_tip.setVisibility(View.GONE);
                }
            }
        });


        Ll_deleteTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cd = new ConnectionDetector(MyRidesDetail.this);
                isInternetPresent = cd.isConnectingToInternet();

                if (isInternetPresent) {
                    postRequest_Tip(Iconstant.tip_remove_url, "Remove");
                } else {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });
        Iv_panic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("----------------panic onclick method-----------------");
                panic();
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
        Ll_share_Ride = (LinearLayout) findViewById(R.id.my_rides_detail_share_layout);



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

        Et_tip_Amount = (EditText) findViewById(R.id.my_rides_detail_tip_editText);
        Bt_tip_Apply = (Button) findViewById(R.id.my_rides_detail_tip_apply_button);
        Rl_main_tip = (RelativeLayout) findViewById(R.id.my_rides_detail_tip_top_layout);
        Rl_tip = (RelativeLayout) findViewById(R.id.my_rides_detail_tip_layout);
        Cb_tip = (CheckBox) findViewById(R.id.my_rides_detail_tip_checkBox);
        Tv_tipAmount = (TextView) findViewById(R.id.my_rides_detail_tip_amount_textView);
        Ll_deleteTip = (LinearLayout) findViewById(R.id.my_rides_detail_tip_amount_remove_layout);

        Iv_panic = (ImageView) findViewById(R.id.panic_image);


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

        final PkDialog mDialog = new PkDialog(MyRidesDetail.this);
        mDialog.setDialogTitle(title);
        mDialog.setDialogMessage(alert);
        mDialog.setPositiveButton(getResources().getString(R.string.action_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();

                completejob_dialog.dismiss();

            }
        });
        mDialog.show();

    }

    private void shareTrip() {
        completejob_dialog  = new MaterialDialog(MyRidesDetail.this);
        View view = LayoutInflater.from(MyRidesDetail.this).inflate(R.layout.share_trip_popup, null);
        Et_share_trip_mobileno = (EditText)view.findViewById(R.id.sharetrip_mobilenoEt);
        Button Bt_Submit = (Button)view.findViewById(R.id.jsharetrip_popup_submit);
        Button Bt_Cancel = (Button)view.findViewById(R.id.sharetrip_popup_cancel);
        completejob_dialog.setView(view).show();
        Bt_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetPresent){
                    share_trip_postRequest_MyRides(MyRidesDetail.this, Iconstant.share_trip_url, "jobcomplete");
                    System.out.println("--------------sharetrip url-------------------" + Iconstant.share_trip_url);
                }else{
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });

        Bt_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completejob_dialog.dismiss();
            }
        });

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

    private void panic(){
        System.out.println("----------------panic method-----------------");
        View view = View.inflate(MyRidesDetail.this, R.layout.panic_page, null);
        final MaterialDialog dialog = new MaterialDialog(MyRidesDetail.this);
        dialog.setContentView(view).setNegativeButton(getResources().getString(R.string.my_rides_detail_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                }
        ).show();
        Button call_police = (Button) view.findViewById(R.id.call_police_button);
        Button call_fire = (Button) view.findViewById(R.id.call_fireservice_button);
        Button call_ambulance = (Button) view.findViewById(R.id.call_ambulance_button);
        call_police.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + "+100"));
                startActivity(callIntent);
            }
        });

        call_fire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + "101"));
                startActivity(callIntent);
            }
        });
        call_ambulance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + "108"));
                startActivity(callIntent);
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
        System.out.println("-------------MyRide Detail user_id----------------" + UserID);
        System.out.println("-------------MyRide Detail ride_id----------------" + SrideId_intent);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);
        jsonParams.put("ride_id", SrideId_intent);

        mRequest = new ServiceRequest(MyRidesDetail.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("-------------MyRide Detail Response----------------" + response);

                String Sstatus = "";
                Currency currencycode;

                try {
                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    if (Sstatus.equalsIgnoreCase("1")) {
                        JSONObject response_object = object.getJSONObject("response");
                        if (response_object.length() > 0) {

                            Object check_details_object = response_object.get("details");
                            if (check_details_object instanceof JSONObject) {

                                JSONObject detail_object = response_object.getJSONObject("details");
                                if (detail_object.length() > 0) {
                                    itemlist.clear();
                                    MyRideDetailPojo pojo = new MyRideDetailPojo();

                                    currencycode = Currency.getInstance(getLocale(detail_object.getString("currency")));
                                    currencySymbol = currencycode.getSymbol();

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


                                    Object check_pickup_object = detail_object.get("pickup");
                                    if (check_pickup_object instanceof JSONObject) {

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
                                    }


                                    if (detail_object.getString("ride_status").equalsIgnoreCase("Completed") || detail_object.getString("ride_status").equalsIgnoreCase("Finished")) {
                                        pojo.setDrop(getResources().getString(R.string.my_rides_detail_drop_textview) + " " + " " + detail_object.getString("drop_date"));


                                        Object check_summary_object = detail_object.get("summary");
                                        if (check_summary_object instanceof JSONObject) {

                                            JSONObject summary_object = detail_object.getJSONObject("summary");
                                            if (summary_object.length() > 0) {
                                                pojo.setRideDistance(summary_object.getString("ride_distance"));
                                                pojo.setTimeTaken(summary_object.getString("ride_duration"));
                                                pojo.setWaitTime(summary_object.getString("waiting_duration"));

                                                isSummaryAvailable = true;
                                            } else {
                                                isSummaryAvailable = false;
                                            }
                                        }


                                        Object check_fare_object = detail_object.get("fare");
                                        if (check_fare_object instanceof JSONObject) {

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

                                    }

                                    itemlist.add(pojo);
                                }
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
                                    Tv_tipAmount.setText(itemlist.get(0).getCurrrencySymbol() + itemlist.get(0).getTip_amount());

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

                                    if (itemlist.get(0).getTip_amount().equalsIgnoreCase("0")) {
                                        Ll_deleteTip.setVisibility(View.GONE);
                                        Rl_main_tip.setVisibility(View.VISIBLE);
                                        Rl_tip.setVisibility(View.GONE);
                                    } else {
                                        Ll_deleteTip.setVisibility(View.VISIBLE);
                                        Rl_main_tip.setVisibility(View.GONE);
                                        Rl_tip.setVisibility(View.GONE);
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
                            }

                            if (itemlist.get(0).getDoCancelAction().equalsIgnoreCase("1")) {
                                Ll_cancelTrip.setVisibility(View.VISIBLE);
                                Ll_payment.setVisibility(View.GONE);
                                Ll_mailInvoice.setVisibility(View.GONE);
                                Ll_reportIssue.setVisibility(View.GONE);
                                Rl_favorite.setVisibility(View.GONE);
                                Rl_main_tip.setVisibility(View.GONE);
                                Rl_tip.setVisibility(View.GONE);
                                Ll_deleteTip.setVisibility(View.GONE);
                                Cb_tip.setChecked(false);
                            }

                            if (itemlist.get(0).getRideStatus().equalsIgnoreCase("Completed")) {
                                Ll_cancelTrip.setVisibility(View.GONE);
                                Ll_payment.setVisibility(View.GONE);
                                Ll_mailInvoice.setVisibility(View.VISIBLE);
                                Ll_reportIssue.setVisibility(View.VISIBLE);
                                Rl_favorite.setVisibility(View.VISIBLE);
                                Rl_main_tip.setVisibility(View.GONE);
                                Rl_tip.setVisibility(View.GONE);
                                Ll_deleteTip.setVisibility(View.GONE);
                                Cb_tip.setChecked(false);

                                //Programmatically making textView to left of layout
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                                params.addRule(RelativeLayout.LEFT_OF, R.id.my_rides_detail_favorite_layout);
                                params.addRule(RelativeLayout.CENTER_VERTICAL, R.id.my_rides_detail_favorite_layout);
                                Tv_rideStatus.setLayoutParams(params);
                            }


                            //------Show and Hide the Button Layout------
                            if (itemlist.get(0).getPay_status().equalsIgnoreCase("Pending") || itemlist.get(0).getPay_status().equalsIgnoreCase("Processing") || itemlist.get(0).getDoCancelAction().equalsIgnoreCase("1") || itemlist.get(0).getRideStatus().equalsIgnoreCase("Completed")||itemlist.get(0).getRideStatus().equalsIgnoreCase("Onride")) {
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
                                Ll_share_Ride.setVisibility(View.VISIBLE);
                            } else {
                                Ll_trackRide.setVisibility(View.GONE);
                                Ll_share_Ride.setVisibility(View.GONE);
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

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });
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
        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);

        mRequest = new ServiceRequest(MyRidesDetail.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("-------------MyRide Cancel Reason Response----------------" + response);

                String Sstatus = "";

                try {
                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    if (Sstatus.equalsIgnoreCase("1")) {
                        JSONObject response_object = object.getJSONObject("response");
                        if (response_object.length() > 0) {

                            Object check_reason_object = response_object.get("reason");
                            if (check_reason_object instanceof JSONArray) {

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

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });
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
        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("email", Semail);
        jsonParams.put("ride_id", SrideId);

        mRequest = new ServiceRequest(MyRidesDetail.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

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

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });
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

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);
        jsonParams.put("title", Et_dialog_FavouriteTitle.getText().toString());
        jsonParams.put("latitude", Str_LocationLatitude);
        jsonParams.put("longitude", Str_LocationLongitude);
        jsonParams.put("address", Tv_address.getText().toString());

        mRequest = new ServiceRequest(MyRidesDetail.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

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

                        final PkDialog mDialog = new PkDialog(MyRidesDetail.this);
                        mDialog.setDialogTitle(getResources().getString(R.string.action_success));
                        mDialog.setDialogMessage(Smessage);
                        mDialog.setPositiveButton(getResources().getString(R.string.action_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDialog.dismiss();
                                favourite_dialog.dismiss();
                                Iv_favorite.setImageResource(R.drawable.heart_red_icon);
                                itemlist.get(0).setIsFavLocation("1");
                            }
                        });
                        mDialog.show();

                    } else {
                        Alert(getResources().getString(R.string.alert_label_title), Smessage);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();
            }

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });
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
        System.out.println("-------------Track Ride ride_id----------------" + SrideId_intent);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("ride_id", SrideId_intent);

        mRequest = new ServiceRequest(MyRidesDetail.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

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

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });
    }


    //----------------------------------Share Trip post reques------------------------
    private void share_trip_postRequest_MyRides(Context mContext, String url,String key) {
        dialog = new Dialog(MyRidesDetail.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));

        System.out.println("------------- ride_id----------------" +SrideId_intent);
        System.out.println("------------- mobile_no----------------" +Et_share_trip_mobileno.getText().toString());

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("ride_id",SrideId_intent);
        jsonParams.put("mobile_no",Et_share_trip_mobileno.getText().toString());

        mRequest = new ServiceRequest(MyRidesDetail.this);
        mRequest.makeServiceRequest(url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {

            @Override
            public void onCompleteListener(String response) {
                Log.e("share trip",response);

                String Str_status = "", Str_response = "";
                System.out.println("sharetrip response-------------"+response);

                try {
                    JSONObject object = new JSONObject(response);
                    Str_status = object.getString("status");
                    Str_response = object.getString("response");

                    if (Str_status.equalsIgnoreCase("1")){

                        Alert(getResources().getString(R.string.action_success), Str_response);

                    }else{
                        Alert(getResources().getString(R.string.action_error), Str_response);

                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                dialog.dismiss();
            }

            @Override
            public void onErrorListener() {

                dialog.dismiss();

            }


        });

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

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("ride_id", SrideId_intent);
        if (tipStatus.equalsIgnoreCase("Apply")) {
            jsonParams.put("tips_amount", Et_tip_Amount.getText().toString());
        }

        mRequest = new ServiceRequest(MyRidesDetail.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("-------------tip Response----------------" + response);
                String sStatus = "", sResponse = "", sTipAmount = "", sTotalBill = "";
                try {

                    JSONObject object = new JSONObject(response);
                    sStatus = object.getString("status");
                    if (sStatus.equalsIgnoreCase("1")) {

                        JSONObject response_Object = object.getJSONObject("response");
                        sTipAmount = response_Object.getString("tips_amount");
                        sTotalBill = response_Object.getString("total");

                        if (tipStatus.equalsIgnoreCase("Apply")) {
                            Tv_tipAmount.setText(currencySymbol + sTipAmount);
                            Tv_totalBill.setText(currencySymbol + sTotalBill);
                            Rl_main_tip.setVisibility(View.GONE);
                            Rl_tip.setVisibility(View.GONE);
                            Ll_deleteTip.setVisibility(View.VISIBLE);
                        } else {

                            Tv_totalBill.setText(currencySymbol + sTotalBill);
                            Tv_tipAmount.setText(currencySymbol + sTipAmount);
                            Cb_tip.setChecked(false);
                            Et_tip_Amount.setText("");
                            Rl_main_tip.setVisibility(View.VISIBLE);
                            Ll_deleteTip.setVisibility(View.GONE);
                        }

                    } else {
                        sResponse = object.getString("response");
                        Alert(getResources().getString(R.string.alert_label_title), sResponse);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
