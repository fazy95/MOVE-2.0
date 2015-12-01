package com.cabily.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cabily.HockeyApp.ActivityHockeyApp;
import com.cabily.adapter.MyRideCancelTripAdapter;
import com.cabily.iconstant.Iconstant;
import com.cabily.pojo.CancelTripPojo;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;
import com.mylibrary.volley.AppController;
import com.mylibrary.volley.VolleyErrorResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Prem Kumar and Anitha on 11/5/2015.
 */
public class TrackRideCancelTrip extends ActivityHockeyApp {
    private RelativeLayout back;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private static Context context;
    private SessionManager session;
    private String UserID = "";

    private StringRequest postrequest;
    Dialog dialog;
    ArrayList<CancelTripPojo> itemlist;
    MyRideCancelTripAdapter adapter;
    private ExpandableHeightListView listview;
    private String SrideId_intent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myride_cancel_trip);
        context = getApplicationContext();
        initialize();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cd = new ConnectionDetector(TrackRideCancelTrip.this);
                isInternetPresent = cd.isConnectingToInternet();

                if (isInternetPresent) {
                    cancel_MyRide(Iconstant.cancel_myride_url, itemlist.get(position).getReasonId());
                } else {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });

    }

    private void initialize() {
        session = new SessionManager(TrackRideCancelTrip.this);
        cd = new ConnectionDetector(TrackRideCancelTrip.this);
        isInternetPresent = cd.isConnectingToInternet();

        back = (RelativeLayout) findViewById(R.id.my_rides_cancel_trip_header_back_layout);
        listview = (ExpandableHeightListView) findViewById(R.id.my_rides_cancel_trip_listView);

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        UserID = user.get(SessionManager.KEY_USERID);

        Intent intent = getIntent();
        SrideId_intent = intent.getStringExtra("RideID");
        try {
            Bundle bundleObject = getIntent().getExtras();
            itemlist = (ArrayList<CancelTripPojo>) bundleObject.getSerializable("Reason");
            adapter = new MyRideCancelTripAdapter(TrackRideCancelTrip.this, itemlist);
            listview.setAdapter(adapter);
            listview.setExpanded(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(TrackRideCancelTrip.this);
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

    //-----------------------Cancel Myride Post Request-----------------
    private void cancel_MyRide(String Url, final String reasonId) {
        dialog = new Dialog(TrackRideCancelTrip.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.my_rides_cancel_trip_action_cancel));


        System.out.println("-------------Cancel Myride Url----------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------Cancel Myride Response----------------" + response);

                        String Sstatus = "";

                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if (Sstatus.equalsIgnoreCase("1")) {
                                JSONObject response_object = object.getJSONObject("response");
                                if (response_object.length() > 0) {
                                    String message = response_object.getString("message");
                                    final MaterialDialog dialog = new MaterialDialog(TrackRideCancelTrip.this);
                                    dialog.setTitle(getResources().getString(R.string.action_success))
                                            .setMessage(message)
                                            .setPositiveButton(
                                                    "OK", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            dialog.dismiss();
                                                            finish();
                                                            Intent broadcastIntent = new Intent();
                                                            broadcastIntent.setAction("com.pushnotification.updateBottom_view");
                                                            sendBroadcast(broadcastIntent);
                                                            TrackYourRide.trackyour_ride_class.finish();
                                                            onBackPressed();
                                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                        }
                                                    }
                                            )
                                            .show();
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
                VolleyErrorResponse.volleyError(TrackRideCancelTrip.this, error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",Iconstant.cabily_userAgent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_id", UserID);
                jsonParams.put("ride_id", SrideId_intent);
                jsonParams.put("reason", reasonId);
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
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            return true;
        }
        return false;
    }
}

