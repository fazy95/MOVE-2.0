package com.cabily.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
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
import com.cabily.adapter.RatingAdapter;
import com.cabily.iconstant.Iconstant;
import com.cabily.pojo.RatingPojo;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;
import com.mylibrary.volley.AppController;
import com.mylibrary.volley.VolleyErrorResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Prem Kumar and Anitha on 11/4/2015.
 */
public class MyRideRating extends ActivityHockeyApp
{
    private RelativeLayout skip;
    private RelativeLayout submit;
    private EditText Et_comment;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private SessionManager session;
    private String UserID = "";

    private StringRequest postrequest;
    Dialog dialog;
    ArrayList<RatingPojo> itemlist;
    RatingAdapter adapter;
    private ExpandableHeightListView listview;
    private String SrideId_intent = "";
    private boolean isDataAvailable=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myride_rating);
        initialize();

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("com.pushnotification.updateBottom_view");
                sendBroadcast(broadcastIntent);

                finish();
                onBackPressed();
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               boolean isRatingEmpty=false;

                if(itemlist!=null)
                {
                    for(int i=0;i<itemlist.size();i++)
                    {
                        if(itemlist.get(i).getRatingcount().length()==0 || itemlist.get(i).getRatingcount().equalsIgnoreCase("0.0"))
                        {
                            isRatingEmpty=true;
                        }
                    }


                    if(!isRatingEmpty)
                    {
                        if (isInternetPresent) {


                            System.out.println("------------ride_id-------------"+SrideId_intent);


                            Map<String, String> jsonParams = new HashMap<String, String>();
                            jsonParams.put("comments", Et_comment.getText().toString());
                            jsonParams.put("ratingsFor", "driver");
                            jsonParams.put("ride_id", SrideId_intent);
                            for (int i=0;i<itemlist.size();i++)
                            {
                                jsonParams.put("ratings["+i+"][option_id]", itemlist.get(i).getRatingId());
                                jsonParams.put("ratings["+i+"][option_title]", itemlist.get(i).getRatingName());
                                jsonParams.put("ratings["+i+"][rating]", itemlist.get(i).getRatingcount());
                            }

                            postRequest_SubmitRating(Iconstant.myride_rating_submit_url,jsonParams);
                        }else {
                            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                        }
                    }
                    else
                    {
                        Alert(getResources().getString(R.string.my_rides_rating_header_sorry_textview), getResources().getString(R.string.my_rides_rating_header_enter_all));
                    }

                }
            }
        });
    }

    private void initialize() {
        session = new SessionManager(MyRideRating.this);
        cd = new ConnectionDetector(MyRideRating.this);
        isInternetPresent = cd.isConnectingToInternet();
        itemlist =new ArrayList<RatingPojo>();

        skip = (RelativeLayout) findViewById(R.id.my_rides_rating_header_skip_layout);
        listview = (ExpandableHeightListView) findViewById(R.id.my_rides_rating_listView);
        submit = (RelativeLayout) findViewById(R.id.my_rides_rating_submit_layout);
        Et_comment=(EditText)findViewById(R.id.my_rides_rating_comment_edittext);


        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        UserID = user.get(SessionManager.KEY_USERID);

        Intent intent = getIntent();
        SrideId_intent = intent.getStringExtra("RideID");

        if (isInternetPresent) {
            postRequest_RatingList(Iconstant.myride_rating_url);
        }else {
            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
        }
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(MyRideRating.this);
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

    //-----------------------Rating List Post Request-----------------
    private void postRequest_RatingList(String Url)
    {
        dialog = new Dialog(MyRideRating.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title=(TextView)dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));


        System.out.println("-------------Rating List Url----------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------Rating List Response----------------"+response);

                        String Sstatus = "";
                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if (Sstatus.equalsIgnoreCase("1")) {
                                    JSONArray payment_array = object.getJSONArray("review_options");
                                    if (payment_array.length() > 0) {
                                        itemlist.clear();
                                        for(int i=0;i<payment_array.length();i++)
                                        {
                                            JSONObject reason_object = payment_array.getJSONObject(i);
                                            RatingPojo pojo=new RatingPojo();
                                            pojo.setRatingId(reason_object.getString("option_id"));
                                            pojo.setRatingName(reason_object.getString("option_title"));
                                            pojo.setRatingcount("");
                                            itemlist.add(pojo);
                                        }
                                        isDataAvailable=true;
                                    }
                                    else
                                    {
                                        isDataAvailable=false;
                                    }
                            }


                            if(Sstatus.equalsIgnoreCase("1") && isDataAvailable)
                            {
                                adapter = new RatingAdapter(MyRideRating.this, itemlist);
                                listview.setAdapter(adapter);
                                listview.setExpanded(true);
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
                VolleyErrorResponse.volleyError(MyRideRating.this, error);
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
                jsonParams.put("optionsFor", "driver");
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }



    //-----------------------Submit Rating Post Request-----------------
    private void postRequest_SubmitRating(String Url, final Map<String, String> jsonParams)
    {
        dialog = new Dialog(MyRideRating.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title=(TextView)dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_pleasewait));


        System.out.println("-------------Submit Rating Url----------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("------------Submit Rating Response----------------"+response);

                        String Sstatus = "";
                        try {
                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if (Sstatus.equalsIgnoreCase("1")) {

                                Intent broadcastIntent = new Intent();
                                broadcastIntent.setAction("com.pushnotification.updateBottom_view");
                                sendBroadcast(broadcastIntent);

                                final MaterialDialog dialog = new MaterialDialog(MyRideRating.this);
                                dialog.setTitle(getResources().getString(R.string.action_success))
                                        .setMessage(getResources().getString(R.string.my_rides_rating_submit_successfully))
                                        .setPositiveButton(
                                                "OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();
                                                        finish();
                                                        onBackPressed();
                                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);


                                                    }
                                                }
                                        )
                                        .show();
                            }
                            else
                            {
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
                VolleyErrorResponse.volleyError(MyRideRating.this, error);
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
            //Do nothing
            return true;
        }
        return false;
    }

}
