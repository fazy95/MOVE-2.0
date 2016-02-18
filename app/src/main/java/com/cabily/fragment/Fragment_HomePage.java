package com.cabily.fragment;

/**
 * Created by Prem Kumar on 10/1/2015.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.cabily.HockeyApp.FragmentHockeyApp;
import com.cabily.adapter.BookMyRide_Adapter;
import com.cabily.adapter.SelectCarTypeAdapter;
import com.cabily.app.EstimatePage;
import com.cabily.app.FavoriteList;
import com.cabily.app.LocationSearch;
import com.cabily.app.NavigationDrawer;
import com.cabily.app.TimerPage;
import com.cabily.app.TrackYourRide;
import com.cabily.iconstant.Iconstant;
import com.cabily.pojo.HomePojo;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.HorizontalListView;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mylibrary.dialog.PkDialog;
import com.mylibrary.gps.GPSTracker;
import com.mylibrary.materialprogresswheel.ProgressWheel;
import com.mylibrary.volley.ServiceRequest;
import com.mylibrary.xmpp.ChatService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import me.drakeet.materialdialog.MaterialDialog;


public class Fragment_HomePage extends FragmentHockeyApp implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private int search_status = 0;
    private String SdestinationLatitude = "";
    private String SdestinationLongitude = "";
    private String SdestinationLocation = "";
    private TextView tv_apply;
    private RelativeLayout drawer_layout;
    private RelativeLayout address_layout, favorite_layout, bottom_layout;
    private RelativeLayout loading_layout;
    private RelativeLayout alert_layout;
    private TextView alert_textview;
    private ImageView center_marker, currentLocation_image;
    private TextView map_address,destination_address;
    private RelativeLayout rideLater_layout, rideNow_layout;
    private TextView rideLater_textview, rideNow_textview;
    private RelativeLayout Rl_Confirm_Back;
    private Context context;
    private ProgressWheel progressWheel;
    private TextView Tv_walletAmount;
    private TextView Tv_marker_time, Tv_marker_min;
    private LinearLayout Ll_marker_time;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private GoogleMap googleMap;
    private MarkerOptions marker;
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    private CardView book_cardview_destination_address_layout;

    private ServiceRequest mRequest;
    private SessionManager session;
    private String UserID = "", CategoryID = "";
    private String CarAvailable = "";
    private String ScarType = "";
    private String selectedType = "";
    GPSTracker gps;
    String SselectedAddress = "";
    String Sselected_latitude = "", Sselected_longitude = "";

    ArrayList<HomePojo> driver_list = new ArrayList<HomePojo>();
    ArrayList<HomePojo> category_list = new ArrayList<HomePojo>();
    ArrayList<HomePojo> ratecard_list = new ArrayList<HomePojo>();
    private boolean driver_status = false;
    private boolean category_status = false;
    private boolean ratecard_status = false;
    private boolean main_response_status = false;

    private double MyCurrent_lat = 0.0, MyCurrent_long = 0.0;
    private double Recent_lat = 0.0, Recent_long = 0.0;

    private BookMyRide_Adapter adapter;
    private HorizontalListView listview;

    private RelativeLayout ridenow_option_layout;
    private RelativeLayout carType_layout, pickTime_layout;
    private LinearLayout ratecard_layout, estimate_layout, coupon_layout;
    private TextView tv_carType, tv_pickuptime, tv_coupon_label;

    private SimpleDateFormat mFormatter = new SimpleDateFormat("MMM/dd,hh:mm aa");
    private SimpleDateFormat coupon_mFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat coupon_time_mFormatter = new SimpleDateFormat("hh:mm aa");
    private SimpleDateFormat mTime_Formatter = new SimpleDateFormat("HH");

    private static View rootview;

    //------Declaration for Coupon code-----
    private RelativeLayout coupon_apply_layout, coupon_loading_layout,coupon_allowance_layout;
    private MaterialDialog coupon_dialog;
    private EditText coupon_edittext;
    private String coupon_selectedDate = "";
    private String coupon_selectedTime = "";
    private String Str_couponCode = "";
    private TextView coupon_allowance;

    //------Declaration for Confirm Ride-----
    private String response_time = "", riderId = "";
    Dialog dialog;
    private int timer_request_code = 100;
    private int placeSearch_request_code = 200;
    private int dest_placeSearch_request_code = 500;
    private int favoriteList_request_code = 300;

    BroadcastReceiver logoutReciver;
    private boolean ratecard_clicked = true;


    //-----Declaration For Enabling Gps-------
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    PendingResult<LocationSettingsResult> result;
    final static int REQUEST_LOCATION = 299;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootview != null) {
            ViewGroup parent = (ViewGroup) rootview.getParent();
            if (parent != null)
                parent.removeView(rootview);
        }
        try {
            rootview = inflater.inflate(R.layout.homepage, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        context = getActivity();
        initialize(rootview);
        initializeMap();
        //Start XMPP Chat Service
        ChatService.startUserAction(getActivity());
        // Finishing the activity using broadcast
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.app.logout");
        filter.addAction("com.pushnotification.updateBottom_view");
        logoutReciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.app.logout")) {
                    getActivity().finish();
                } else if (intent.getAction().equals("com.pushnotification.updateBottom_view")) {
                    googleMap.getUiSettings().setAllGesturesEnabled(true);
                    ridenow_option_layout.setVisibility(View.GONE);
                    center_marker.setImageResource(R.drawable.pickup_map_pointer);
                    center_marker.setEnabled(true);
                    listview.setVisibility(View.VISIBLE);
                    rideLater_textview.setText(getResources().getString(R.string.home_label_ride_later));
                    rideNow_textview.setText(getResources().getString(R.string.home_label_ride_now));
                    currentLocation_image.setClickable(true);
                    pickTime_layout.setEnabled(true);
                    drawer_layout.setEnabled(true);
                    address_layout.setEnabled(true);
                    //destination_address_layout.setVisibility(View.VISIBLE);
                    //destination_address_layout.setEnabled(true);
                    favorite_layout.setEnabled(true);
                    NavigationDrawer.enableSwipeDrawer();
                    double Dlatitude = gps.getLatitude();
                    double Dlongitude = gps.getLongitude();
                    // Move the camera to last position with a zoom level
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Dlatitude, Dlongitude)).zoom(17).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

            }
        };
        getActivity().registerReceiver(logoutReciver, filter);


        drawer_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationDrawer.openDrawer();
            }
        });

        address_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                search_status = 0;
                Intent intent = new Intent(getActivity(), LocationSearch.class);
                startActivityForResult(intent, placeSearch_request_code);
                getActivity().overridePendingTransition(R.anim.slideup, R.anim.slidedown);
            }
        });

        book_cardview_destination_address_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                search_status = 1;
                Intent intent = new Intent(getActivity(), LocationSearch.class);
                startActivityForResult(intent, placeSearch_request_code);
                getActivity().overridePendingTransition(R.anim.slideup, R.anim.slidedown);
            }
        });

        favorite_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                search_status = 0;
                if (map_address.getText().toString().length() > 0) {
                    Intent intent = new Intent(getActivity(), FavoriteList.class);
                    intent.putExtra("SelectedAddress", SselectedAddress);
                    intent.putExtra("SelectedLatitude", Sselected_latitude);
                    intent.putExtra("SelectedLongitude", Sselected_longitude);
                    startActivityForResult(intent, favoriteList_request_code);
                    getActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
                } else {
                    Alert(getActivity().getResources().getString(R.string.alert_label_title), getActivity().getResources().getString(R.string.favorite_list_label_select_location));
                }

            }
        });

        carType_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                select_carType_Dialog();
            }
        });


        pickTime_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new SlideDateTimePicker.Builder(getActivity().getSupportFragmentManager())
                        .setListener(Sublistener)
                        .setInitialDate(new Date())
                        .setMinDate(new Date())
                                //.setMaxDate(maxDate)
                                //.setIs24HourTime(true)
                        .setTheme(SlideDateTimePicker.HOLO_LIGHT)
                        .setIndicatorColor(Color.parseColor("#F83C6F"))
                        .build()
                        .show();
            }
        });


        ratecard_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ratecard_clicked) {
                    ratecard_clicked = false;
                    showRateCard();
                }

            }
        });

        estimate_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EstimatePage.class);
                intent.putExtra("UserId", UserID);
                intent.putExtra("PickUp", map_address.getText().toString());
                intent.putExtra("PickUp_Lat", String.valueOf(Recent_lat));
                intent.putExtra("PickUp_Long", String.valueOf(Recent_long));
                intent.putExtra("Category", CategoryID);
                intent.putExtra("Type", selectedType);
                intent.putExtra("PickUp_Date", coupon_selectedDate);
                intent.putExtra("PickUp_Time", coupon_selectedTime);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        Rl_Confirm_Back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                //Enable and Disable RideNow Button
                if (CarAvailable.equalsIgnoreCase("no cabs")) {

                    Ll_marker_time.setVisibility(View.GONE);
                    Tv_marker_time.setVisibility(View.GONE);
                    Tv_marker_min.setVisibility(View.GONE);
                    center_marker.setImageResource(R.drawable.pickup_map_pointer_no_car_available);
                    rideNow_textview.setTextColor(Color.parseColor("#848484"));
                    rideNow_layout.setClickable(false);
                } else {

                    Ll_marker_time.setVisibility(View.VISIBLE);
                    Tv_marker_time.setVisibility(View.VISIBLE);
                    Tv_marker_min.setVisibility(View.VISIBLE);
                    center_marker.setImageResource(R.drawable.pickup_map_pointer);
                    rideNow_textview.setTextColor(Color.parseColor("#FFFFFF"));
                    rideNow_layout.setClickable(true);

                    Tv_marker_time.setText(CarAvailable.replace("min", "").replace("mins", ""));
                }

                Animation animFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
                ridenow_option_layout.startAnimation(animFadeOut);
                ridenow_option_layout.setVisibility(View.GONE);
                center_marker.setEnabled(true);

                googleMap.getUiSettings().setAllGesturesEnabled(true);
                listview.setVisibility(View.VISIBLE);
                rideLater_textview.setText(getResources().getString(R.string.home_label_ride_later));
                rideNow_textview.setText(getResources().getString(R.string.home_label_ride_now));
                currentLocation_image.setClickable(true);
                pickTime_layout.setEnabled(true);
                drawer_layout.setEnabled(true);
                address_layout.setEnabled(true);
                //destination_address_layout.setVisibility(View.VISIBLE);
                //destination_address_layout.setEnabled(true);
                favorite_layout.setEnabled(true);
                NavigationDrawer.enableSwipeDrawer();
            }
        });

        coupon_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showCoupon();
            }
        });

        rideLater_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                book_cardview_destination_address_layout.setVisibility(View.GONE);

                if (rideLater_textview.getText().toString().equalsIgnoreCase(getResources().getString(R.string.home_label_ride_later))) {

                    selectedType = "1";
                    Str_couponCode = "";

                    new SlideDateTimePicker.Builder(getActivity().getSupportFragmentManager())
                            .setListener(listener)
                            .setInitialDate(new Date())
                            .setMinDate(new Date())
                                    //.setMaxDate(maxDate)
                                    //.setIs24HourTime(true)
                            .setTheme(SlideDateTimePicker.HOLO_LIGHT)
                            .setIndicatorColor(Color.parseColor("#F83C6F"))
                            .build()
                            .show();

                } else if (rideLater_textview.getText().toString().equalsIgnoreCase(getResources().getString(R.string.home_label_cancel))) {

                    //Enable and Disable RideNow Button
                    if (CarAvailable.equalsIgnoreCase("no cabs")) {

                        Ll_marker_time.setVisibility(View.GONE);
                        Tv_marker_time.setVisibility(View.GONE);
                        Tv_marker_min.setVisibility(View.GONE);
                        center_marker.setImageResource(R.drawable.pickup_map_pointer_no_car_available);
                        rideNow_textview.setTextColor(Color.parseColor("#848484"));
                        rideNow_layout.setClickable(false);
                    } else {

                        Ll_marker_time.setVisibility(View.VISIBLE);
                        Tv_marker_time.setVisibility(View.VISIBLE);
                        Tv_marker_min.setVisibility(View.VISIBLE);
                        center_marker.setImageResource(R.drawable.pickup_map_pointer);
                        rideNow_textview.setTextColor(Color.parseColor("#FFFFFF"));
                        rideNow_layout.setClickable(true);

                        Tv_marker_time.setText(CarAvailable.replace("min", "").replace("mins", ""));
                    }

                    Animation animFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
                    ridenow_option_layout.startAnimation(animFadeOut);
                    ridenow_option_layout.setVisibility(View.GONE);
                    center_marker.setEnabled(true);

                    googleMap.getUiSettings().setAllGesturesEnabled(true);
                    listview.setVisibility(View.VISIBLE);
                    rideLater_textview.setText(getResources().getString(R.string.home_label_ride_later));
                    rideNow_textview.setText(getResources().getString(R.string.home_label_ride_now));
                    currentLocation_image.setClickable(true);
                    pickTime_layout.setEnabled(true);
                    drawer_layout.setEnabled(true);
                    address_layout.setEnabled(true);
                    //destination_address_layout.setVisibility(View.VISIBLE);
                   // destination_address_layout.setEnabled(true);
                    favorite_layout.setEnabled(true);
                    NavigationDrawer.enableSwipeDrawer();
                }

            }
        });

        rideNow_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                if (rideNow_textview.getText().toString().equalsIgnoreCase(getResources().getString(R.string.home_label_ride_now))) {
                    selectedType = "0";
                    Str_couponCode = "";
                    book_cardview_destination_address_layout.setVisibility(View.VISIBLE);
                    if (CarAvailable.equalsIgnoreCase("no cabs")) {
                        Alert(getActivity().getResources().getString(R.string.alert_label_title), getActivity().getResources().getString(R.string.alert_label_content1));
                    } else {

                        tv_coupon_label.setText(getResources().getString(R.string.ridenow_label_coupon));
                        tv_coupon_label.setTextColor(Color.parseColor("#4e4e4e"));


                        //-------getting current date and time---------
                        coupon_selectedDate = coupon_mFormatter.format(new Date());
                        coupon_selectedTime = coupon_time_mFormatter.format(new Date());
                        String displaytime = CarAvailable + " " + getResources().getString(R.string.home_label_fromNow);

                        //--------Disabling the map functionality---------
                        googleMap.getUiSettings().setAllGesturesEnabled(false);
                        currentLocation_image.setClickable(false);

                        Animation animFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
                        ridenow_option_layout.startAnimation(animFadeIn);
                        ridenow_option_layout.setVisibility(View.VISIBLE);
                        center_marker.setImageResource(R.drawable.pickup_map_pointer_pin);
                        center_marker.setEnabled(false);

                        listview.setVisibility(View.GONE);
                        rideLater_textview.setText(getResources().getString(R.string.home_label_cancel));
                        rideNow_textview.setText(getResources().getString(R.string.home_label_confirm));

                        tv_carType.setText(ScarType);
                        tv_pickuptime.setText(displaytime);

                        //----Disabling onClick Listener-----
                        pickTime_layout.setEnabled(false);
                        drawer_layout.setEnabled(false);
                        address_layout.setEnabled(false);
                        //destination_address_layout.setVisibility(View.VISIBLE);
                       // destination_address_layout.setEnabled(false);
                        favorite_layout.setEnabled(false);
                        NavigationDrawer.disableSwipeDrawer();
                    }
                } else if (rideNow_textview.getText().toString().equalsIgnoreCase(getResources().getString(R.string.home_label_confirm))) {
                    cd = new ConnectionDetector(getActivity());
                    isInternetPresent = cd.isConnectingToInternet();
                    book_cardview_destination_address_layout.setVisibility(View.GONE);
                    if (isInternetPresent) {
                       /* HashMap<String, String> code = session.getCouponCode();
                        String coupon = code.get(SessionManager.KEY_COUPON_CODE);*/

                        riderId="";
                        ConfirmRideRequest(Iconstant.confirm_ride_url, Str_couponCode, coupon_selectedDate, coupon_selectedTime, selectedType, CategoryID, map_address.getText().toString(), String.valueOf(Recent_lat), String.valueOf(Recent_long), "",destination_address.getText().toString(),SdestinationLatitude,SdestinationLongitude);
                    } else {
                        Alert(getActivity().getResources().getString(R.string.alert_label_title), getActivity().getResources().getString(R.string.alert_nointernet));
                    }
                }
            }
        });


        center_marker.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!CarAvailable.equalsIgnoreCase("no cabs")) {
                    selectedType = "0";

                    //-------getting current date and time---------
                    coupon_selectedDate = coupon_mFormatter.format(new Date());
                    coupon_selectedTime = coupon_time_mFormatter.format(new Date());
                    String displaytime = CarAvailable + " " + getResources().getString(R.string.home_label_fromNow);

                    //--------Disabling the map functionality---------
                    googleMap.getUiSettings().setAllGesturesEnabled(false);
                    currentLocation_image.setClickable(false);

                    Animation animFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
                    ridenow_option_layout.startAnimation(animFadeIn);
                    ridenow_option_layout.setVisibility(View.VISIBLE);
                    center_marker.setImageResource(R.drawable.pickup_map_pointer_pin);
                    center_marker.setEnabled(false);

                    listview.setVisibility(View.GONE);
                    rideLater_textview.setText(getResources().getString(R.string.home_label_cancel));
                    rideNow_textview.setText(getResources().getString(R.string.home_label_confirm));

                    tv_carType.setText(ScarType);
                    tv_pickuptime.setText(displaytime);

                    //----Disabling onClick Listener-----
                    pickTime_layout.setEnabled(false);
                    drawer_layout.setEnabled(false);
                    address_layout.setEnabled(false);
                    //destination_address_layout.setVisibility(View.GONE);
                    //destination_address_layout.setEnabled(false);
                    favorite_layout.setEnabled(false);
                    NavigationDrawer.disableSwipeDrawer();
                }
            }
        });


        listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                CarAvailable = category_list.get(position).getCat_time();
                CategoryID = category_list.get(position).getCat_id();
                ScarType = category_list.get(position).getCat_name();

                cd = new ConnectionDetector(getActivity());
                isInternetPresent = cd.isConnectingToInternet();

                if (Recent_lat != 0.0) {
                    googleMap.clear();

                    if (isInternetPresent) {
                        if (mRequest != null) {
                            mRequest.cancelRequest();
                        }

                        PostRequest(Iconstant.BookMyRide_url, Recent_lat, Recent_long);
                    } else {
                        alert_layout.setVisibility(View.VISIBLE);
                        alert_textview.setText(getResources().getString(R.string.alert_nointernet));
                    }
                }

                adapter.notifyDataSetChanged();
            }
        });

        currentLocation_image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(getActivity());
                isInternetPresent = cd.isConnectingToInternet();
                gps = new GPSTracker(getActivity());

                if (gps.canGetLocation() && gps.isgpsenabled()) {

                    MyCurrent_lat = gps.getLatitude();
                    MyCurrent_long = gps.getLongitude();

                    if (mRequest != null) {
                        mRequest.cancelRequest();
                    }

                    // Move the camera to last position with a zoom level
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(MyCurrent_lat, MyCurrent_long)).zoom(17).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                } else {
                    enableGpsService();
                    //Toast.makeText(getActivity(), "GPS not Enabled !!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        OnCameraChangeListener mOnCameraChangeListener = new OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                double latitude = cameraPosition.target.latitude;
                double longitude = cameraPosition.target.longitude;

                cd = new ConnectionDetector(getActivity());
                isInternetPresent = cd.isConnectingToInternet();

                Log.e("camerachange lat-->", "" + latitude);
                Log.e("on_camera_change lon-->", "" + longitude);

                if (latitude != 0.0) {
                    googleMap.clear();

                    Recent_lat = latitude;
                    Recent_long = longitude;

                    if (isInternetPresent) {
                        if (mRequest != null) {
                            mRequest.cancelRequest();
                        }
                        PostRequest(Iconstant.BookMyRide_url, latitude, longitude);
                    } else {
                        alert_layout.setVisibility(View.VISIBLE);
                        alert_textview.setText(getResources().getString(R.string.alert_nointernet));
                        bottom_layout.setVisibility(View.GONE);
                    }
                }
            }
        };

        if (CheckPlayService()) {
            googleMap.setOnCameraChangeListener(mOnCameraChangeListener);
            googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    String tittle = marker.getTitle();
                    return true;
                }
            });
        } else {
            Toast.makeText(getActivity(), "Install Google Play service To View Location !!!", Toast.LENGTH_LONG).show();
        }

        return rootview;
    }

    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.book_my_ride_mapview)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getActivity(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
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

        if (gps.canGetLocation() && gps.isgpsenabled()) {
            double Dlatitude = gps.getLatitude();
            double Dlongitude = gps.getLongitude();

            MyCurrent_lat = Dlatitude;
            MyCurrent_long = Dlongitude;

            Recent_lat = Dlatitude;
            Recent_long = Dlongitude;

            // Move the camera to last position with a zoom level
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Dlatitude, Dlongitude)).zoom(17).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else {

            enableGpsService();
           /* alert_layout.setVisibility(View.VISIBLE);
            alert_textview.setText(getResources().getString(R.string.alert_gpsEnable));*/
        }
    }

    private void initialize(View rooView) {
        cd = new ConnectionDetector(getActivity());
        isInternetPresent = cd.isConnectingToInternet();
        session = new SessionManager(getActivity());
        gps = new GPSTracker(getActivity());

        drawer_layout = (RelativeLayout) rooView.findViewById(R.id.book_navigation_layout);
        address_layout = (RelativeLayout) rooView.findViewById(R.id.book_navigation_address_layout);

        //destination_address_layout = (RelativeLayout) rooView.findViewById(R.id.book_navigation_destination_address_layout);

        favorite_layout = (RelativeLayout) rooView.findViewById(R.id.book_navigation_favorite_layout);
        bottom_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_bottom_layout);
        map_address = (TextView) rooView.findViewById(R.id.book_navigation_search_address);

        destination_address = (TextView) rooView.findViewById(R.id.book_navigation_destination_address_search_address);

        loading_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_loading_layout);
        center_marker = (ImageView) rooView.findViewById(R.id.book_my_ride_center_marker);
        alert_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_alert_layout);
        alert_textview = (TextView) rooView.findViewById(R.id.book_my_ride_alert_textView);
        currentLocation_image = (ImageView) rooView.findViewById(R.id.book_current_location_imageview);
        rideLater_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_rideLater_layout);
        rideNow_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_rideNow_layout);
        rideLater_textview = (TextView) rooView.findViewById(R.id.book_my_ride_rideLater_textView);
        rideNow_textview = (TextView) rooView.findViewById(R.id.book_my_ride_rideNow_textview);
        listview = (HorizontalListView) rooView.findViewById(R.id.book_my_ride_listview);
        ridenow_option_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_ridenow_option_layout);
        carType_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_cabtype_layout);
        pickTime_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_pickup_layout);
        ratecard_layout = (LinearLayout) rooView.findViewById(R.id.book_my_ride_ratecard_layout);
        estimate_layout = (LinearLayout) rooView.findViewById(R.id.book_my_ride_estimate_layout);
        coupon_layout = (LinearLayout) rooView.findViewById(R.id.book_my_ride_applycoupon_layout);
        tv_carType = (TextView) rooView.findViewById(R.id.cartype_textview);
        tv_pickuptime = (TextView) rooView.findViewById(R.id.pickup_textview);
        tv_coupon_label = (TextView) rooView.findViewById(R.id.applycoupon_label);
        progressWheel = (ProgressWheel) rooView.findViewById(R.id.book_my_ride_progress_wheel);
        Tv_walletAmount = (TextView) rootview.findViewById(R.id.book_my_ride_wallet_amount_textView);
        Rl_Confirm_Back = (RelativeLayout) rootview.findViewById(R.id.book_my_ride_confirm_header_back_layout);

        Tv_marker_time = (TextView) rootview.findViewById(R.id.book_my_ride_confirm_header_car_time_textView);
        Tv_marker_min = (TextView) rootview.findViewById(R.id.book_my_ride_confirm_header_car_time_min_textView);
        Ll_marker_time = (LinearLayout) rooView.findViewById(R.id.book_my_ride_marker_time_layout);
        book_cardview_destination_address_layout =(CardView)rooView.findViewById(R.id.book_cardview_destination_address_layout);
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        UserID = user.get(SessionManager.KEY_USERID);

        HashMap<String, String> wallet = session.getWalletAmount();
        String sWalletAmount = wallet.get(SessionManager.KEY_WALLET_AMOUNT);

        Tv_walletAmount.setText(sWalletAmount);

        HashMap<String, String> cat = session.getCategoryID();
        String sCategoryId = cat.get(SessionManager.KEY_CATEGORY_ID);

        if (sCategoryId.length() > 0) {
            CategoryID = cat.get(SessionManager.KEY_CATEGORY_ID);
        } else {
            CategoryID = user.get(SessionManager.KEY_CATEGORY);
        }

    }


    //-------------------Show Coupon Code Method--------------------
    private void showCoupon() {
        coupon_dialog = new MaterialDialog(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.coupon_code_dialog, null);

        tv_apply = (TextView) view.findViewById(R.id.couponcode_apply_textView);
        TextView tv_cancel = (TextView) view.findViewById(R.id.couponcode_cancel_textView);
        final TextView tv_nointernet = (TextView) view.findViewById(R.id.couponcode_nointernet_textView);
        coupon_edittext = (EditText) view.findViewById(R.id.couponcode_editText);
        coupon_apply_layout = (RelativeLayout) view.findViewById(R.id.couponcode_apply_layout);
        coupon_loading_layout = (RelativeLayout) view.findViewById(R.id.couponcode_loading_layout);
        coupon_allowance_layout = (RelativeLayout) view.findViewById(R.id.couponcode_allowance_amount_layout);
        coupon_allowance = (TextView) view.findViewById(R.id.couponcode_allowance_textview);

        HashMap<String, String> code = session.getCouponCode();
        String coupon = code.get(SessionManager.KEY_COUPON_CODE);
        if (!coupon.isEmpty()) {
            coupon_edittext.setText(coupon);
            tv_apply.setText(getResources().getString(R.string.couponcode_label_remove));

        }

        coupon_apply_layout.setVisibility(View.VISIBLE);
        coupon_loading_layout.setVisibility(View.GONE);
        coupon_edittext.addTextChangedListener(EditorWatcher);




        coupon_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(coupon_edittext.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });
        tv_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                coupon_dialog.dismiss();
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
        });
        tv_apply.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (coupon_edittext.length() == 0) {
                    coupon_edittext.setHint(getResources().getString(R.string.couponcode_label_invalid_code));
                    coupon_edittext.setHintTextColor(Color.RED);
                    Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                    coupon_edittext.startAnimation(shake);
                } else {
                    cd = new ConnectionDetector(getActivity());
                    isInternetPresent = cd.isConnectingToInternet();

                    if (isInternetPresent) {
                        tv_nointernet.setVisibility(View.INVISIBLE);
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        if (getResources().getString(R.string.couponcode_label_apply).equalsIgnoreCase(tv_apply.getText().toString())) {
                            CouponCodeRequest(Iconstant.couponCode_apply_url, coupon_edittext.getText().toString(), coupon_selectedDate);
                        }else{
                            session.setCouponCode(" ");
                            coupon_edittext.setText("");
                            tv_apply.setText(getResources().getString(R.string.couponcode_label_apply));
                            coupon_allowance_layout.setVisibility(View.GONE);
                            tv_coupon_label.setText(getResources().getString(R.string.ridenow_label_coupon));
                        }
                    } else {
                        tv_nointernet.setVisibility(View.VISIBLE);
                    }

                }
            }
        });
        coupon_dialog.setView(view).show();
    }


    //-------------------Show RateCard Method--------------------
    private void showRateCard() {
        if (ratecard_status) {
            final MaterialDialog dialog = new MaterialDialog(getActivity());
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.ratecard_dialog, null);

            TextView tv_cartype = (TextView) view.findViewById(R.id.ratecard_caretype_textview);
            TextView tv_firstprice = (TextView) view.findViewById(R.id.first_price_textView);
            TextView tv_firstKm = (TextView) view.findViewById(R.id.first_km_textView);
            TextView tv_afterprice = (TextView) view.findViewById(R.id.after_price_textView);
            TextView tv_afterKm = (TextView) view.findViewById(R.id.after_km_textView);
            TextView tv_otherprice = (TextView) view.findViewById(R.id.other_price_textView);
            TextView tv_otherKm = (TextView) view.findViewById(R.id.other_km_textView);
            TextView tv_note = (TextView) view.findViewById(R.id.ratecard_note_textview);
            TextView tv_ok = (TextView) view.findViewById(R.id.ratecard_ok_textview);

            if (ratecard_list.size() > 0) {
                Currency currencycode = Currency.getInstance(getLocale(ratecard_list.get(0).getCurrencyCode()));

                tv_cartype.setText(ratecard_list.get(0).getRate_cartype());
                tv_firstprice.setText(currencycode.getSymbol() + ratecard_list.get(0).getMinfare_amt());
                tv_firstKm.setText(ratecard_list.get(0).getMinfare_km());
                tv_afterprice.setText(currencycode.getSymbol() + ratecard_list.get(0).getAfterfare_amt());
                tv_afterKm.setText(ratecard_list.get(0).getAfterfare_km());
                tv_otherprice.setText(currencycode.getSymbol() + ratecard_list.get(0).getOtherfare_amt());
                tv_otherKm.setText(ratecard_list.get(0).getOtherfare_km());
                tv_note.setText(ratecard_list.get(0).getRate_note());
            }

            tv_ok.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    dialog.dismiss();
                    ratecard_clicked = true;
                }
            });
            dialog.setView(view).show();
        }
    }

    //-------------------Show CarType Method--------------------
    private void select_carType_Dialog() {
        final MaterialDialog dialog = new MaterialDialog(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.home_cartype_dialog, null);

        ListView car_listview = (ListView) view.findViewById(R.id.car_type_dialog_listView);

        SelectCarTypeAdapter car_adapter = new SelectCarTypeAdapter(getActivity(), category_list);
        car_listview.setAdapter(car_adapter);
        car_adapter.notifyDataSetChanged();

        dialog.setTitle(getActivity().getResources().getString(R.string.car_type_select_dialog_label_carType));
        dialog.setPositiveButton(getActivity().getResources().getString(R.string.car_type_select_dialog_label_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                }
        );

        car_listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                CategoryID = category_list.get(position).getCat_id();
                SelectCar_Request(Iconstant.BookMyRide_url, Recent_lat, Recent_long);
            }
        });
        dialog.setView(view).show();
    }

    //----------------------Code for TextWatcher-------------------------
    private final TextWatcher EditorWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            //clear error symbol after entering text
            if (coupon_edittext.getText().length() > 0) {
                coupon_edittext.setHint("");
            }
        }
    };


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


    //-----------Check Google Play Service--------
    private boolean CheckPlayService() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                final Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, getActivity(), REQUEST_CODE_RECOVER_PLAY_SERVICES);
                if (dialog == null) {
                    Toast.makeText(getActivity(), "incompatible version of Google Play Services", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    //-------------Method to get Complete Address------------
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            } else {
                Log.e("Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Current loction address", "Canont get Address!");
        }
        return strAdd;
    }


    //--------------Alert Method-----------
    private void Alert(String title, String alert) {

        final PkDialog mDialog = new PkDialog(getActivity());
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


    //----------------DatePicker Listener------------
    private SlideDateTimeListener listener = new SlideDateTimeListener() {
        @Override
        public void onDateTimeSet(Date date) {

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, 1);
            Date d = cal.getTime();
            String currenttime = mTime_Formatter.format(d);
            String selecedtime = mTime_Formatter.format(date);
            String displaytime = mFormatter.format(date);

            System.out.println("-----------------current date---------------------" + currenttime);
            System.out.println("-----------------selected date---------------------" + selecedtime);

            if (selecedtime.equalsIgnoreCase("00")) {
                selecedtime = "24";
            }

            if (Integer.parseInt(currenttime) <= Integer.parseInt(selecedtime)) {

                //Enable and Disable RideNow Button
                if (selectedType.equalsIgnoreCase("1")) {
                    rideNow_textview.setTextColor(Color.parseColor("#FFFFFF"));
                    rideNow_layout.setClickable(true);
                }

                coupon_selectedDate = coupon_mFormatter.format(date);
                coupon_selectedTime = coupon_time_mFormatter.format(date);


                //--------Disabling the map functionality---------
                googleMap.getUiSettings().setAllGesturesEnabled(false);
                currentLocation_image.setClickable(false);

                pickTime_layout.setEnabled(true);
                drawer_layout.setEnabled(false);
                address_layout.setEnabled(false);
                //destination_address_layout.setVisibility(View.GONE);
                //destination_address_layout.setEnabled(false);
                favorite_layout.setEnabled(false);

                Animation animFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
                ridenow_option_layout.startAnimation(animFadeIn);
                ridenow_option_layout.setVisibility(View.VISIBLE);
                center_marker.setImageResource(R.drawable.pickup_map_pointer_pin);
                center_marker.setEnabled(false);

                listview.setVisibility(View.GONE);
                rideLater_textview.setText(getResources().getString(R.string.home_label_cancel));
                rideNow_textview.setText(getResources().getString(R.string.home_label_confirm));

                tv_carType.setText(ScarType);
                tv_pickuptime.setText(displaytime);
                NavigationDrawer.disableSwipeDrawer();

            } else {
                Alert(getActivity().getResources().getString(R.string.alert_label_ridelater_title), getActivity().getResources().getString(R.string.alert_label_ridelater_content));
            }
        }

        // Optional cancel listener
        @Override
        public void onDateTimeCancel() {
            Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
        }
    };


    //----------------DatePicker Secondary Listener------------
    private SlideDateTimeListener Sublistener = new SlideDateTimeListener() {
        @Override
        public void onDateTimeSet(Date date) {

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, 1);
            Date d = cal.getTime();
            String currenttime = mTime_Formatter.format(d);
            String selecedtime = mTime_Formatter.format(date);
            String displaytime = mFormatter.format(date);


            if (selecedtime.equalsIgnoreCase("00")) {
                selecedtime = "24";
            }

            if (Integer.parseInt(currenttime) <= Integer.parseInt(selecedtime)) {
                coupon_selectedDate = coupon_mFormatter.format(date);
                coupon_selectedTime = coupon_time_mFormatter.format(date);

                tv_pickuptime.setText(displaytime);
            } else {
                Alert(getActivity().getResources().getString(R.string.alert_label_ridelater_title), getActivity().getResources().getString(R.string.alert_label_ridelater_content));
            }
        }

        // Optional cancel listener
        @Override
        public void onDateTimeCancel() {
        }
    };


    //-------------------AsynTask To get the current Address----------------
    private void PostRequest(String Url, final double latitude, final double longitude) {
        //loading_layout.setVisibility(View.VISIBLE);
        progressWheel.setVisibility(View.VISIBLE);
        //center_marker.setVisibility(View.GONE);
        rideNow_layout.setEnabled(false);

        System.out.println("--------------Book My ride url-------------------" + Url);

        Sselected_latitude = String.valueOf(latitude);
        Sselected_longitude = String.valueOf(longitude);

        System.out.println("--------------Book My ride UserID-------------------" + UserID);
        System.out.println("--------------Book My ride latitude-------------------" + latitude);
        System.out.println("--------------Book My ride longitude-------------------" + longitude);
        System.out.println("--------------Book My ride CategoryID-------------------" + CategoryID);


        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);
        jsonParams.put("lat", String.valueOf(latitude));
        jsonParams.put("lon", String.valueOf(longitude));
        jsonParams.put("category", CategoryID);

        mRequest = new ServiceRequest(getActivity());
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("--------------Book My ride reponse-------------------" + response);
                String fail_response = "";
                try {
                    JSONObject object = new JSONObject(response);

                    if (object.length() > 0) {
                        if (object.getString("status").equalsIgnoreCase("1")) {

                            JSONObject jobject = object.getJSONObject("response");
                            if (jobject.length() > 0) {
                                for (int i = 0; i < jobject.length(); i++) {

                                    Object check_driver_object = jobject.get("drivers");
                                    if (check_driver_object instanceof JSONArray) {

                                        JSONArray driver_array = jobject.getJSONArray("drivers");
                                        if (driver_array.length() > 0) {
                                            driver_list.clear();

                                            for (int j = 0; j < driver_array.length(); j++) {
                                                JSONObject driver_object = driver_array.getJSONObject(j);

                                                HomePojo pojo = new HomePojo();
                                                pojo.setDriver_lat(driver_object.getString("lat"));
                                                pojo.setDriver_long(driver_object.getString("lon"));

                                                driver_list.add(pojo);
                                            }
                                            driver_status = true;
                                        } else {
                                            driver_list.clear();
                                            driver_status = false;
                                        }
                                    } else {
                                        driver_status = false;
                                    }


                                    Object check_ratecard_object = jobject.get("ratecard");
                                    if (check_ratecard_object instanceof JSONObject) {

                                        JSONObject ratecard_object = jobject.getJSONObject("ratecard");
                                        if (ratecard_object.length() > 0) {
                                            ratecard_list.clear();
                                            HomePojo pojo = new HomePojo();

                                            pojo.setRate_cartype(ratecard_object.getString("category"));
                                            pojo.setRate_note(ratecard_object.getString("note"));
                                            pojo.setCurrencyCode(jobject.getString("currency"));

                                            JSONObject farebreakup_object = ratecard_object.getJSONObject("farebreakup");
                                            if (farebreakup_object.length() > 0) {
                                                JSONObject minfare_object = farebreakup_object.getJSONObject("min_fare");
                                                if (minfare_object.length() > 0) {
                                                    pojo.setMinfare_amt(minfare_object.getString("amount"));
                                                    pojo.setMinfare_km(minfare_object.getString("text"));
                                                }

                                                JSONObject afterfare_object = farebreakup_object.getJSONObject("after_fare");
                                                if (afterfare_object.length() > 0) {
                                                    pojo.setAfterfare_amt(afterfare_object.getString("amount"));
                                                    pojo.setAfterfare_km(afterfare_object.getString("text"));
                                                }

                                                JSONObject otherfare_object = farebreakup_object.getJSONObject("other_fare");
                                                if (otherfare_object.length() > 0) {
                                                    pojo.setOtherfare_amt(otherfare_object.getString("amount"));
                                                    pojo.setOtherfare_km(otherfare_object.getString("text"));
                                                }
                                            }

                                            ratecard_list.add(pojo);
                                            ratecard_status = true;
                                        } else {
                                            ratecard_list.clear();
                                            ratecard_status = false;
                                        }
                                    } else {
                                        ratecard_status = false;
                                    }


                                    Object check_category_object = jobject.get("category");
                                    if (check_category_object instanceof JSONArray) {

                                        JSONArray cat_array = jobject.getJSONArray("category");
                                        if (cat_array.length() > 0) {
                                            category_list.clear();

                                            for (int k = 0; k < cat_array.length(); k++) {

                                                JSONObject cat_object = cat_array.getJSONObject(k);

                                                HomePojo pojo = new HomePojo();
                                                pojo.setCat_name(cat_object.getString("name"));
                                                pojo.setCat_time(cat_object.getString("eta"));
                                                pojo.setCat_id(cat_object.getString("id"));
                                                pojo.setIcon_normal(cat_object.getString("icon_normal"));
                                                pojo.setIcon_active(cat_object.getString("icon_active"));
                                                pojo.setSelected_Cat(jobject.getString("selected_category"));

                                                if (cat_object.getString("id").equals(jobject.getString("selected_category"))) {
                                                    CarAvailable = cat_object.getString("eta");
                                                    ScarType = cat_object.getString("name");
                                                }

                                                category_list.add(pojo);
                                            }

                                            category_status = true;
                                        } else {
                                            category_list.clear();
                                            category_status = false;
                                        }
                                    } else {
                                        category_status = false;
                                    }
                                }
                            }

                            main_response_status = true;
                        } else {
                            fail_response = object.getString("response");
                            main_response_status = false;
                        }

                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                if (main_response_status) {
                    alert_layout.setVisibility(View.GONE);
                    bottom_layout.setVisibility(View.VISIBLE);

                    if (driver_status) {
                        for (int i = 0; i < driver_list.size(); i++) {
                            double Dlatitude = Double.parseDouble(driver_list.get(i).getDriver_lat());
                            double Dlongitude = Double.parseDouble(driver_list.get(i).getDriver_long());

                            // create marker double Dlatitude = gps.getLatitude();
                            MarkerOptions marker = new MarkerOptions().position(new LatLng(Dlatitude, Dlongitude));
                            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon));

                            // adding marker
                            googleMap.addMarker(marker);
                        }
                    } else {
                        googleMap.clear();
                    }

                    if (category_status) {

                        //Enable and Disable RideNow Button
                        if (CarAvailable.equalsIgnoreCase("no cabs")) {

                            Ll_marker_time.setVisibility(View.GONE);
                            Tv_marker_time.setVisibility(View.GONE);
                            Tv_marker_min.setVisibility(View.GONE);
                            center_marker.setImageResource(R.drawable.pickup_map_pointer_no_car_available);
                            rideNow_textview.setTextColor(Color.parseColor("#848484"));
                            rideNow_layout.setClickable(false);
                        } else {

                            Ll_marker_time.setVisibility(View.VISIBLE);
                            Tv_marker_time.setVisibility(View.VISIBLE);
                            Tv_marker_min.setVisibility(View.VISIBLE);
                            center_marker.setImageResource(R.drawable.pickup_map_pointer);
                            rideNow_textview.setTextColor(Color.parseColor("#FFFFFF"));
                            rideNow_layout.setClickable(true);

                            Tv_marker_time.setText(CarAvailable.replace("min", "").replace("mins", ""));
                        }

                        listview.setVisibility(View.VISIBLE);

                        adapter = new BookMyRide_Adapter(getActivity(), category_list);
                        listview.setAdapter(adapter);
                    } else {
                        listview.setVisibility(View.GONE);
                    }

                } else {
                    alert_layout.setVisibility(View.VISIBLE);
                    bottom_layout.setVisibility(View.GONE);
                    alert_textview.setText(fail_response);
                }

                String address = getCompleteAddressString(latitude, longitude);
                map_address.setText(address);
                SselectedAddress = address;

                progressWheel.setVisibility(View.GONE);
                //loading_layout.setVisibility(View.GONE);
                //center_marker.setVisibility(View.VISIBLE);
                rideNow_layout.setEnabled(true);
            }

            @Override
            public void onErrorListener() {
                progressWheel.setVisibility(View.GONE);
                //loading_layout.setVisibility(View.GONE);
                //center_marker.setVisibility(View.VISIBLE);
                rideNow_layout.setEnabled(true);

                alert_layout.setVisibility(View.VISIBLE);
                bottom_layout.setVisibility(View.GONE);
            }
        });
    }


    //-------------------Coupon Code Post Request----------------

    private void CouponCodeRequest(String Url, final String code, final String pickpudate) {
        System.out.println("--------------coupon code url-------------------" + Url);

        coupon_apply_layout.setVisibility(View.GONE);
        coupon_loading_layout.setVisibility(View.VISIBLE);


        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);
        jsonParams.put("code", code);
        jsonParams.put("pickup_date", pickpudate);

        mRequest = new ServiceRequest(getActivity());
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("--------------coupon code reponse-------------------" + response);

                try {
                    JSONObject object = new JSONObject(response);
                    if (object.length() > 0) {
                        String status = object.getString("status");
                        if (status.equalsIgnoreCase("1")) {

                            JSONObject result_object = object.getJSONObject("response");

                            coupon_apply_layout.setVisibility(View.VISIBLE);
                            coupon_loading_layout.setVisibility(View.GONE);


                            String code = result_object.getString("code");
                            String type = result_object.getString("discount_type");
                            String discount = result_object.getString("discount_amount");

                            Str_couponCode = code;
                            session.setCouponCode(code);
                            coupon_allowance_layout.setVisibility(View.VISIBLE);
                            if(getResources().getString(R.string.couponcode_allowance_type).equalsIgnoreCase(type)) {
                                coupon_allowance.setText(getResources().getString(R.string.couponcode_label_allowance_text1) +discount + getResources().getString(R.string.couponcode_label_allowance_text2));
                            }
                            else
                            {
                                coupon_allowance.setText(getResources().getString(R.string.couponcode_label_allowance_text1) +discount + getResources().getString(R.string.couponcode_label_allowance_text3));
                            }
                            tv_apply.setText(getResources().getString(R.string.couponcode_label_remove));
                            tv_coupon_label.setText(Str_couponCode);//getResources().getString(R.string.couponcode_label_verifed)
                            tv_coupon_label.setTextColor(getResources().getColor(R.color.darkgreen_color));

                        } else {

                            Str_couponCode = "";
                            session.setCouponCode(Str_couponCode);

                            coupon_apply_layout.setVisibility(View.VISIBLE);
                            coupon_loading_layout.setVisibility(View.GONE);
                            coupon_allowance_layout.setVisibility(View.GONE);

                            coupon_edittext.setText("");
                            coupon_edittext.setHint(getResources().getString(R.string.couponcode_label_invalid_code));
                            coupon_edittext.setHintTextColor(Color.RED);
                            Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                            coupon_edittext.startAnimation(shake);
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorListener() {
                coupon_apply_layout.setVisibility(View.VISIBLE);
                coupon_loading_layout.setVisibility(View.GONE);
                coupon_dialog.dismiss();
            }
        });
    }


    //-------------------Confirm Ride Post Request----------------

    private void ConfirmRideRequest(String Url, final String code, final String pickUpDate, final String pickup_time, final String type, final String category, final String pickup_location, final String pickup_lat, final String pickup_lon, final String try_value,final String destination_location, final String destination_lat, final String destination_lon) {

        dialog = new Dialog(getActivity());
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView loading = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        loading.setText(getResources().getString(R.string.action_pleasewait));

        System.out.println("--------------Confirm Ride url-------------------" + Url);


        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);
        jsonParams.put("code", code);
        jsonParams.put("pickup_date", pickUpDate);
        jsonParams.put("pickup_time", pickup_time);
        jsonParams.put("type", type);
        jsonParams.put("category", category);
        jsonParams.put("pickup", pickup_location);
        jsonParams.put("pickup_lat", pickup_lat);
        jsonParams.put("pickup_lon", pickup_lon);
        jsonParams.put("ride_id", riderId);
        jsonParams.put("drop_loc", destination_location);
        jsonParams.put("drop_lat", destination_lat);
        jsonParams.put("drop_lon", destination_lon);



        System.out.println("---------------user_id----------" + UserID);
        System.out.println("---------------code----------" + code);
        System.out.println("---------------pickpudate----------" + pickUpDate);
        System.out.println("---------------pickup_time----------" + pickup_time);
        System.out.println("---------------type----------" + type);
        System.out.println("---------------category----------" + category);
        System.out.println("---------------pickup----------" + pickup_location);
        System.out.println("---------------pickup_lat----------" + pickup_lat);
        System.out.println("---------------pickup_lon----------" + pickup_lon);
        System.out.println("---------------try----------" + try_value);
        System.out.println("---------------riderId----------" + riderId);
        System.out.println("---------------destination_location----------" + destination_location);
        System.out.println("---------------destination_lat----------" + destination_lat);
        System.out.println("---------------destination_lon----------" + destination_lon);

        mRequest = new ServiceRequest(getActivity());
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("--------------Confirm Ride reponse-------------------" + response);

                String selected_type = "", Sacceptance = "";
                String Str_driver_id = "", Str_driver_name = "", Str_driver_email = "", Str_driver_image = "", Str_driver_review = "",
                        Str_driver_lat = "", Str_driver_lon = "", Str_min_pickup_duration = "", Str_ride_id = "", Str_phone_number = "",
                        Str_vehicle_number = "", Str_vehicle_model = "";
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.length() > 0) {
                        String status = object.getString("status");
                        if (status.equalsIgnoreCase("1")) {
                            JSONObject response_object = object.getJSONObject("response");

                            selected_type = response_object.getString("type");
                            Sacceptance = object.getString("acceptance");
                            if (Sacceptance.equalsIgnoreCase("No")) {
                                response_time = response_object.getString("response_time");
                            }
                            riderId = response_object.getString("ride_id");


                            if (Sacceptance.equalsIgnoreCase("Yes")) {
                                JSONObject driverObject = response_object.getJSONObject("driver_profile");

                                Str_driver_id = driverObject.getString("driver_id");
                                Str_driver_name = driverObject.getString("driver_name");
                                Str_driver_email = driverObject.getString("driver_email");
                                Str_driver_image = driverObject.getString("driver_image");
                                Str_driver_review = driverObject.getString("driver_review");
                                Str_driver_lat = driverObject.getString("driver_lat");
                                Str_driver_lon = driverObject.getString("driver_lon");
                                Str_min_pickup_duration = driverObject.getString("min_pickup_duration");
                                Str_ride_id = driverObject.getString("ride_id");
                                Str_phone_number = driverObject.getString("phone_number");
                                Str_vehicle_number = driverObject.getString("vehicle_number");
                                Str_vehicle_model = driverObject.getString("vehicle_model");
                            }


                            if (selected_type.equalsIgnoreCase("1")) {

                                final PkDialog mDialog = new PkDialog(getActivity());
                                mDialog.setDialogTitle(getActivity().getResources().getString(R.string.action_success));
                                mDialog.setDialogMessage(getActivity().getResources().getString(R.string.ridenow_label_confirm_success));
                                mDialog.setPositiveButton(getResources().getString(R.string.action_ok), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mDialog.dismiss();


                                        //Enable and Disable RideNow Button
                                        if (CarAvailable.equalsIgnoreCase("no cabs")) {

                                            Ll_marker_time.setVisibility(View.GONE);
                                            Tv_marker_time.setVisibility(View.GONE);
                                            Tv_marker_min.setVisibility(View.GONE);
                                            center_marker.setImageResource(R.drawable.pickup_map_pointer_no_car_available);
                                            rideNow_textview.setTextColor(Color.parseColor("#848484"));
                                            rideNow_layout.setClickable(false);
                                        } else {

                                            Ll_marker_time.setVisibility(View.VISIBLE);
                                            Tv_marker_time.setVisibility(View.VISIBLE);
                                            Tv_marker_min.setVisibility(View.VISIBLE);
                                            center_marker.setImageResource(R.drawable.pickup_map_pointer);
                                            rideNow_textview.setTextColor(Color.parseColor("#FFFFFF"));
                                            rideNow_layout.setClickable(true);

                                            Tv_marker_time.setText(CarAvailable.replace("min", "").replace("mins", ""));
                                        }


                                        //---------Hiding the bottom layout after success request--------
                                        googleMap.getUiSettings().setAllGesturesEnabled(true);

                                        Animation animFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
                                        ridenow_option_layout.startAnimation(animFadeOut);
                                        ridenow_option_layout.setVisibility(View.GONE);
                                        center_marker.setEnabled(true);

                                        listview.setVisibility(View.VISIBLE);
                                        rideLater_textview.setText(getResources().getString(R.string.home_label_ride_later));
                                        rideNow_textview.setText(getResources().getString(R.string.home_label_ride_now));
                                        currentLocation_image.setClickable(true);
                                        pickTime_layout.setEnabled(true);
                                        drawer_layout.setEnabled(true);
                                        address_layout.setEnabled(true);
                                        //destination_address_layout.setVisibility(View.VISIBLE);
                                        //destination_address_layout.setEnabled(true);
                                        favorite_layout.setEnabled(true);
                                        NavigationDrawer.enableSwipeDrawer();
                                    }
                                });
                                mDialog.show();

                            } else if (selected_type.equalsIgnoreCase("0")) {
                                if (Sacceptance.equalsIgnoreCase("Yes")) {
                                    //Move to ride Detail page
                                    Intent i = new Intent(getActivity(), TrackYourRide.class);
                                    i.putExtra("driverID", Str_driver_id);
                                    i.putExtra("driverName", Str_driver_name);
                                    i.putExtra("driverImage", Str_driver_image);
                                    i.putExtra("driverRating", Str_driver_review);
                                    i.putExtra("driverLat", Str_driver_lat);
                                    i.putExtra("driverLong", Str_driver_lon);
                                    i.putExtra("driverTime", Str_min_pickup_duration);
                                    i.putExtra("rideID", Str_ride_id);
                                    i.putExtra("driverMobile", Str_phone_number);
                                    i.putExtra("driverCar_no", Str_vehicle_number);
                                    i.putExtra("driverCar_model", Str_vehicle_model);
                                    i.putExtra("userLat", pickup_lat);
                                    i.putExtra("userLong", pickup_lon);
                                    startActivity(i);
                                    getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                } else {
                                    Intent intent = new Intent(getActivity(), TimerPage.class);
                                    intent.putExtra("Time", response_time);
                                    intent.putExtra("retry_count", try_value);
                                    intent.putExtra("ride_ID", riderId);
                                    startActivityForResult(intent, timer_request_code);
                                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                }
                            }
                        } else {
                            String Sresponse = object.getString("response");
                            Alert(getActivity().getResources().getString(R.string.alert_label_title), Sresponse);
                        }
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


    //-------------------Delete Ride Post Request----------------

    private void DeleteRideRequest(String Url) {

        dialog = new Dialog(getActivity());
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView loading = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        loading.setText(getResources().getString(R.string.action_pleasewait));

        System.out.println("--------------Delete Ride url-------------------" + Url);


        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);
        jsonParams.put("ride_id", riderId);

        mRequest = new ServiceRequest(getActivity());
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("--------------Delete Ride reponse-------------------" + response);

                try {
                    JSONObject object = new JSONObject(response);
                    if (object.length() > 0) {
                        String status = object.getString("status");
                        String response_value = object.getString("response");
                        if (status.equalsIgnoreCase("1")) {
                            riderId = "";
                            Alert(getActivity().getResources().getString(R.string.action_success), response_value);
                        } else {
                            Alert(getActivity().getResources().getString(R.string.alert_label_title), response_value);
                        }


                        //---------Hiding the bottom layout after cancel request--------
                        googleMap.getUiSettings().setAllGesturesEnabled(true);

                        Animation animFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
                        ridenow_option_layout.startAnimation(animFadeOut);
                        ridenow_option_layout.setVisibility(View.GONE);
                        center_marker.setImageResource(R.drawable.pickup_map_pointer);
                        center_marker.setEnabled(true);

                        listview.setVisibility(View.VISIBLE);
                        rideLater_textview.setText(getResources().getString(R.string.home_label_ride_later));
                        rideNow_textview.setText(getResources().getString(R.string.home_label_ride_now));
                        currentLocation_image.setClickable(true);
                        pickTime_layout.setEnabled(true);
                        drawer_layout.setEnabled(true);
                        address_layout.setEnabled(true);
                       // destination_address_layout.setVisibility(View.VISIBLE);
                       // destination_address_layout.setEnabled(true);
                        favorite_layout.setEnabled(true);
                        NavigationDrawer.enableSwipeDrawer();
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


    //-------------------Select Car Type Request---------------
    private void SelectCar_Request(String Url, final double latitude, final double longitude) {

        final Dialog mdialog = new Dialog(getActivity());
        mdialog.getWindow();
        mdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mdialog.setContentView(R.layout.custom_loading);
        mdialog.setCanceledOnTouchOutside(false);
        mdialog.show();

        TextView dialog_title = (TextView) mdialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_updating));

        System.out.println("--------------Select Car Type url-------------------" + Url);


        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("user_id", UserID);
        jsonParams.put("lat", String.valueOf(latitude));
        jsonParams.put("lon", String.valueOf(longitude));
        jsonParams.put("category", CategoryID);

        mRequest = new ServiceRequest(getActivity());
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("--------------Select Car Type reponse-------------------" + response);
                String fail_response = "";

                try {
                    JSONObject object = new JSONObject(response);

                    if (object.length() > 0) {
                        if (object.getString("status").equalsIgnoreCase("1")) {
                            JSONObject jobject = object.getJSONObject("response");
                            if (jobject.length() > 0) {
                                for (int i = 0; i < jobject.length(); i++) {

                                    Object check_driver_object = jobject.get("drivers");
                                    if (check_driver_object instanceof JSONArray) {
                                        JSONArray driver_array = jobject.getJSONArray("drivers");
                                        if (driver_array.length() > 0) {
                                            driver_list.clear();

                                            for (int j = 0; j < driver_array.length(); j++) {
                                                JSONObject driver_object = driver_array.getJSONObject(j);

                                                HomePojo pojo = new HomePojo();
                                                pojo.setDriver_lat(driver_object.getString("lat"));
                                                pojo.setDriver_long(driver_object.getString("lon"));

                                                driver_list.add(pojo);
                                            }
                                            driver_status = true;
                                        } else {
                                            driver_list.clear();
                                            driver_status = false;
                                        }
                                    } else {
                                        driver_status = false;
                                    }


                                    Object check_ratecard_object = jobject.get("ratecard");
                                    if (check_ratecard_object instanceof JSONObject) {

                                        JSONObject ratecard_object = jobject.getJSONObject("ratecard");
                                        if (ratecard_object.length() > 0) {
                                            ratecard_list.clear();
                                            HomePojo pojo = new HomePojo();

                                            pojo.setRate_cartype(ratecard_object.getString("category"));
                                            pojo.setRate_note(ratecard_object.getString("note"));
                                            pojo.setCurrencyCode(jobject.getString("currency"));

                                            JSONObject farebreakup_object = ratecard_object.getJSONObject("farebreakup");
                                            if (farebreakup_object.length() > 0) {
                                                JSONObject minfare_object = farebreakup_object.getJSONObject("min_fare");
                                                if (minfare_object.length() > 0) {
                                                    pojo.setMinfare_amt(minfare_object.getString("amount"));
                                                    pojo.setMinfare_km(minfare_object.getString("text"));
                                                }

                                                JSONObject afterfare_object = farebreakup_object.getJSONObject("after_fare");
                                                if (afterfare_object.length() > 0) {
                                                    pojo.setAfterfare_amt(afterfare_object.getString("amount"));
                                                    pojo.setAfterfare_km(afterfare_object.getString("text"));
                                                }

                                                JSONObject otherfare_object = farebreakup_object.getJSONObject("other_fare");
                                                if (otherfare_object.length() > 0) {
                                                    pojo.setOtherfare_amt(otherfare_object.getString("amount"));
                                                    pojo.setOtherfare_km(otherfare_object.getString("text"));
                                                }
                                            }

                                            ratecard_list.add(pojo);
                                            ratecard_status = true;
                                        } else {
                                            ratecard_list.clear();
                                            ratecard_status = false;
                                        }
                                    } else {
                                        ratecard_status = false;
                                    }


                                    Object check_category_object = jobject.get("category");
                                    if (check_category_object instanceof JSONArray) {

                                        JSONArray cat_array = jobject.getJSONArray("category");
                                        if (cat_array.length() > 0) {
                                            category_list.clear();

                                            for (int k = 0; k < cat_array.length(); k++) {

                                                JSONObject cat_object = cat_array.getJSONObject(k);

                                                HomePojo pojo = new HomePojo();
                                                pojo.setCat_name(cat_object.getString("name"));
                                                pojo.setCat_time(cat_object.getString("eta"));
                                                pojo.setCat_id(cat_object.getString("id"));
                                                pojo.setIcon_normal(cat_object.getString("icon_normal"));
                                                pojo.setIcon_active(cat_object.getString("icon_active"));
                                                pojo.setSelected_Cat(jobject.getString("selected_category"));

                                                if (cat_object.getString("id").equals(jobject.getString("selected_category"))) {
                                                    CarAvailable = cat_object.getString("eta");
                                                    ScarType = cat_object.getString("name");
                                                }

                                                category_list.add(pojo);
                                            }

                                            category_status = true;
                                        } else {
                                            category_list.clear();
                                            category_status = false;
                                        }
                                    } else {
                                        category_status = false;
                                    }

                                }
                            }

                            main_response_status = true;
                        } else {
                            fail_response = object.getString("response");
                            main_response_status = false;
                        }

                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                if (main_response_status) {
                    tv_carType.setText(ScarType);
                    if (driver_status) {
                        googleMap.clear();
                        for (int i = 0; i < driver_list.size(); i++) {
                            double Dlatitude = Double.parseDouble(driver_list.get(i).getDriver_lat());
                            double Dlongitude = Double.parseDouble(driver_list.get(i).getDriver_long());

                            // create marker double Dlatitude = gps.getLatitude();
                            MarkerOptions marker = new MarkerOptions().position(new LatLng(Dlatitude, Dlongitude));
                            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon));

                            // adding marker
                            googleMap.addMarker(marker);
                        }
                    } else {
                        googleMap.clear();
                    }


                    if (category_status) {

                        //Enable and Disable RideNow Button
                        if (CarAvailable.equalsIgnoreCase("no cabs")) {
                            rideNow_textview.setTextColor(Color.parseColor("#848484"));
                            rideNow_layout.setClickable(false);
                        } else {
                            rideNow_textview.setTextColor(Color.parseColor("#FFFFFF"));
                            rideNow_layout.setClickable(true);
                        }

                        listview.setVisibility(View.GONE);

                        adapter = new BookMyRide_Adapter(getActivity(), category_list);
                        listview.setAdapter(adapter);
                    } else {
                        listview.setVisibility(View.GONE);
                    }

                    mdialog.dismiss();

                } else {
                    mdialog.dismiss();
                }
            }

            @Override
            public void onErrorListener() {
                mdialog.dismiss();
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        System.out.println("--------------onActivityResult requestCode----------------" + requestCode);

        // code to get country name
        if (requestCode == timer_request_code && resultCode == Activity.RESULT_OK && data != null) {
            String ride_accepted = data.getStringExtra("Accepted_or_Not");
            String retry_count = data.getStringExtra("Retry_Count");

            if (retry_count.equalsIgnoreCase("1") && ride_accepted.equalsIgnoreCase("not")) {


                final PkDialog mDialog = new PkDialog(getActivity());
                mDialog.setDialogTitle(getResources().getString(R.string.timer_label_alert_sorry));
                mDialog.setDialogMessage(getResources().getString(R.string.timer_label_alert_content));
                mDialog.setPositiveButton(getResources().getString(R.string.timer_label_alert_retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();

                        cd = new ConnectionDetector(getActivity());
                        isInternetPresent = cd.isConnectingToInternet();

                        if (isInternetPresent) {
                            HashMap<String, String> code = session.getCouponCode();
                            String coupon = code.get(SessionManager.KEY_COUPON_CODE);

                            ConfirmRideRequest(Iconstant.confirm_ride_url, coupon, coupon_selectedDate, coupon_selectedTime, selectedType, CategoryID, map_address.getText().toString(), String.valueOf(Recent_lat), String.valueOf(Recent_long), "2",destination_address.getText().toString(),SdestinationLatitude,SdestinationLongitude);
                        } else {
                            Alert(getActivity().getResources().getString(R.string.alert_label_title), getActivity().getResources().getString(R.string.alert_nointernet));
                        }
                    }
                });
                mDialog.setNegativeButton(getResources().getString(R.string.timer_label_alert_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();

                        cd = new ConnectionDetector(getActivity());
                        isInternetPresent = cd.isConnectingToInternet();

                        if (isInternetPresent) {
                            DeleteRideRequest(Iconstant.delete_ride_url);
                        } else {
                            Alert(getActivity().getResources().getString(R.string.alert_label_title), getActivity().getResources().getString(R.string.alert_nointernet));
                        }
                    }
                });
                mDialog.show();

            } else if (retry_count.equalsIgnoreCase("2") && ride_accepted.equalsIgnoreCase("not")) {
                DeleteRideRequest(Iconstant.delete_ride_url);
            } else if ((retry_count.equalsIgnoreCase("1") || retry_count.equalsIgnoreCase("2")) && ride_accepted.equalsIgnoreCase("Cancelled")) {

                riderId = "";

                //---------Hiding the bottom layout after cancel request--------
                googleMap.getUiSettings().setAllGesturesEnabled(true);

                Animation animFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
                ridenow_option_layout.startAnimation(animFadeOut);
                ridenow_option_layout.setVisibility(View.GONE);
                center_marker.setImageResource(R.drawable.pickup_map_pointer);
                center_marker.setEnabled(true);

                listview.setVisibility(View.VISIBLE);
                rideLater_textview.setText(getResources().getString(R.string.home_label_ride_later));
                rideNow_textview.setText(getResources().getString(R.string.home_label_ride_now));
                currentLocation_image.setClickable(true);
                pickTime_layout.setEnabled(true);
                drawer_layout.setEnabled(true);
                address_layout.setEnabled(true);
                //destination_address_layout.setVisibility(View.VISIBLE);
                //destination_address_layout.setEnabled(true);
                favorite_layout.setEnabled(true);
                NavigationDrawer.enableSwipeDrawer();
            }

        } else if ((requestCode == placeSearch_request_code && resultCode == Activity.RESULT_OK && data != null) ) {

            if(search_status == 0) {
                String SselectedLatitude = data.getStringExtra("Selected_Latitude");
                String SselectedLongitude = data.getStringExtra("Selected_Longitude");
                String SselectedLocation = data.getStringExtra("Selected_Location");

                // Move the camera to last position with a zoom level
                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Double.parseDouble(SselectedLatitude), Double.parseDouble(SselectedLongitude))).zoom(17).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                map_address.setText(SselectedLocation);
            }
            else
            {
                SdestinationLatitude = data.getStringExtra("Selected_Latitude");
                SdestinationLongitude = data.getStringExtra("Selected_Longitude");
                SdestinationLocation = data.getStringExtra("Selected_Location");
                System.out.println("-----------SdestinationLatitude----------------"+SdestinationLatitude);
                System.out.println("-----------SdestinationLongitude----------------"+SdestinationLongitude);
                System.out.println("-----------SdestinationLocation----------------"+SdestinationLocation);

                destination_address.setText(SdestinationLocation);
            }
        }/*else if ((requestCode == dest_placeSearch_request_code && resultCode == Activity.RESULT_OK && data != null) || search_status == 1) {

            SdestinationLatitude = data.getStringExtra("Selected_Latitude");
            SdestinationLongitude = data.getStringExtra("Selected_Longitude");
            SdestinationLocation = data.getStringExtra("Selected_Location");
            System.out.println("-----------SdestinationLatitude----------------"+SdestinationLatitude);
            System.out.println("-----------SdestinationLongitude----------------"+SdestinationLongitude);
            System.out.println("-----------SdestinationLocation----------------"+SdestinationLocation);

            destination_address.setText(SdestinationLocation);
        }*/

        else if (requestCode == favoriteList_request_code && resultCode == Activity.RESULT_OK && data != null) {

            String SselectedLatitude = data.getStringExtra("Selected_Latitude");
            String SselectedLongitude = data.getStringExtra("Selected_Longitude");

            // Move the camera to last position with a zoom level
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Double.parseDouble(SselectedLatitude), Double.parseDouble(SselectedLongitude))).zoom(17).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else if (requestCode == REQUEST_LOCATION) {
            System.out.println("----------inside request location------------------");

            switch (resultCode) {
                case Activity.RESULT_OK: {
                    Toast.makeText(getActivity(), "Location enabled!", Toast.LENGTH_LONG).show();
                    break;
                }
                case Activity.RESULT_CANCELED: {
                    enableGpsService();
                    break;
                }
                default: {
                    break;
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(logoutReciver);
        super.onDestroy();
    }


    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }


    //Enabling Gps Service
    private void enableGpsService() {

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        //...
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        //...
                        break;
                }
            }
        });
    }

}
