package com.cabily.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cabily.HockeyApp.ActivityHockeyApp;
import com.cabily.adapter.PlaceSearchAdapter;
import com.cabily.iconstant.Iconstant;
import com.cabily.pojo.EstimateDetailPojo;
import com.cabily.utils.ConnectionDetector;
import com.casperon.app.cabily.R;
import com.mylibrary.volley.AppController;
import com.mylibrary.volley.VolleyErrorResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Prem Kumar and Anitha on 11/12/2015.
 */
public class LocationSearch extends ActivityHockeyApp {

    private RelativeLayout back;
    private EditText et_search;
    private ListView listview;
    private RelativeLayout alert_layout;
    private TextView alert_textview;
    private TextView tv_emptyText;
    private ProgressBar progresswheel;

    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;

    StringRequest postrequest;
    StringRequest estimate_postrequest;
    Context context;
    ArrayList<String> itemList_location=new ArrayList<String>();
    ArrayList<String> itemList_placeId=new ArrayList<String>();

    PlaceSearchAdapter adapter;
    private boolean isdataAvailable=false;
    private boolean isEstimateAvailable=false;

    private String Slatitude="",Slongitude="",Sselected_location="";

    Dialog dialog;
    ArrayList<EstimateDetailPojo> ratecard_list = new ArrayList<EstimateDetailPojo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_search);
        context = getApplicationContext();
        initialize();


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sselected_location=itemList_location.get(position);

                cd = new ConnectionDetector(LocationSearch.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    LatLongRequest(Iconstant.GetAddressFrom_LatLong_url+itemList_placeId.get(position));
                }
                else
                {
                    alert_layout.setVisibility(View.VISIBLE);
                    alert_textview.setText(getResources().getString(R.string.alert_nointernet));
                }

            }
        });

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                cd = new ConnectionDetector(LocationSearch.this);
                isInternetPresent = cd.isConnectingToInternet();

                if (isInternetPresent) {
                    if (postrequest != null) {
                        postrequest.cancel();
                    }
                    CitySearchRequest(Iconstant.place_search_url + et_search.getText().toString().toLowerCase());
                } else {
                    alert_layout.setVisibility(View.VISIBLE);
                    alert_textview.setText(getResources().getString(R.string.alert_nointernet));
                }

            }
        });

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(et_search);
                }
                return false;
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // close keyboard
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(back.getWindowToken(), 0);

                LocationSearch.this.finish();
                LocationSearch.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private void initialize()
    {
        alert_layout = (RelativeLayout) findViewById(R.id.location_search_alert_layout);
        alert_textview = (TextView) findViewById(R.id.location_search_alert_textView);
        back = (RelativeLayout) findViewById(R.id.location_search_back_layout);
        et_search = (EditText) findViewById(R.id.location_search_editText);
        listview = (ListView) findViewById(R.id.location_search_listView);
        progresswheel = (ProgressBar) findViewById(R.id.location_search_progressBar);
        tv_emptyText = (TextView) findViewById(R.id.location_search_empty_textview);

    }

    private void CloseKeyboard(EditText edittext) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(edittext.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(LocationSearch.this);
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

    //-------------------Search Place Request----------------
    private void CitySearchRequest(String Url) {

        progresswheel.setVisibility(View.VISIBLE);
        System.out.println("--------------Search city url-------------------" + Url);
        postrequest = new StringRequest(Request.Method.GET, Url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                System.out.println("--------------Search city  reponse-------------------" + response);
                String status="";
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.length() > 0) {

                        status=object.getString("status");
                        JSONArray place_array = object.getJSONArray("predictions");
                        if(status.equalsIgnoreCase("OK"))
                        {
                            if(place_array.length()>0)
                            {
                                itemList_location.clear();
                                itemList_placeId.clear();
                                for (int i = 0; i < place_array.length(); i++)
                                {
                                    JSONObject place_object = place_array.getJSONObject(i);
                                    itemList_location.add(place_object.getString("description"));
                                    itemList_placeId.add(place_object.getString("place_id"));
                                }
                                isdataAvailable=true;
                            }
                            else
                            {
                                itemList_location.clear();
                                itemList_placeId.clear();
                                isdataAvailable=false;
                            }
                        }
                        else
                        {
                            itemList_location.clear();
                            itemList_placeId.clear();
                            isdataAvailable=false;
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                progresswheel.setVisibility(View.INVISIBLE);
                alert_layout.setVisibility(View.GONE);
                if(isdataAvailable)
                {
                    tv_emptyText.setVisibility(View.GONE);
                }
                else
                {
                    tv_emptyText.setVisibility(View.VISIBLE);
                }
                adapter=new PlaceSearchAdapter(LocationSearch.this,itemList_location);
                listview.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progresswheel.setVisibility(View.INVISIBLE);
                alert_layout.setVisibility(View.GONE);

                // close keyboard
                CloseKeyboard(et_search);
                VolleyErrorResponse.volleyError(LocationSearch.this, error);
            }
        });
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }


    //-------------------Get Latitude and Longitude from Address(Place ID) Request----------------
    private void LatLongRequest(String Url) {

        dialog = new Dialog(LocationSearch.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title=(TextView)dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_processing));

        System.out.println("--------------LatLong url-------------------" + Url);
        postrequest = new StringRequest(Request.Method.GET, Url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                System.out.println("--------------LatLong  reponse-------------------" + response);
                String status = "";
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.length() > 0) {

                        status = object.getString("status");
                        JSONObject place_object = object.getJSONObject("result");
                        if (status.equalsIgnoreCase("OK")) {
                            if (place_object.length() > 0) {
                                JSONObject geometry_object = place_object.getJSONObject("geometry");
                                if (geometry_object.length() > 0) {
                                    JSONObject location_object = geometry_object.getJSONObject("location");
                                    if (location_object.length() > 0) {
                                        Slatitude = location_object.getString("lat");
                                        Slongitude = location_object.getString("lng");
                                        isdataAvailable = true;
                                    } else {
                                        isdataAvailable = false;
                                    }
                                } else {
                                    isdataAvailable = false;
                                }
                            } else {
                                isdataAvailable = false;
                            }
                        } else {
                            isdataAvailable = false;
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (isdataAvailable) {

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("Selected_Latitude", Slatitude);
                    returnIntent.putExtra("Selected_Longitude", Slongitude);
                    returnIntent.putExtra("Selected_Location", Sselected_location);
                    setResult(RESULT_OK, returnIntent);
                    onBackPressed();
                    overridePendingTransition(R.anim.slideup, R.anim.slidedown);
                    finish();

                } else {
                    dialog.dismiss();
                    Alert(getResources().getString(R.string.alert_label_title), status);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(LocationSearch.this, error);
            }
        });
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

            // close keyboard
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(back.getWindowToken(), 0);

            LocationSearch.this.finish();
            LocationSearch.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return false;
    }
}

