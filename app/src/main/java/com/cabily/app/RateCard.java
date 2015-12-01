package com.cabily.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cabily.HockeyApp.ActivityHockeyApp;
import com.cabily.adapter.RateCardAdapter;
import com.cabily.adapter.RateCardStandardAdapter;
import com.cabily.iconstant.Iconstant;
import com.cabily.pojo.RateCard_CarPojo;
import com.cabily.pojo.RateCard_CardDisplayPojo;
import com.cabily.pojo.RateCard_CityPojo;
import com.cabily.pojo.RateCard_StdPojo;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;
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

import fr.ganfra.materialspinner.MaterialSpinner;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Prem Kumar and Anitha on 10/14/2015.
 */
public class RateCard extends ActivityHockeyApp
{
    private RelativeLayout back;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private SessionManager session;
    private MaterialSpinner city_spinner;
    private MaterialSpinner carType_spinner;
    private ExpandableHeightListView listview,standard_listview;
    private RelativeLayout rate_display_layout;

    private String Sselected_cityID="";
    private StringRequest postrequest,car_postrequest,ratecard_postrequest;
    Dialog dialog;
    private boolean isCityAvailable=false;
    private boolean isCarAvailable=false;
    ArrayList<String> city_array=new ArrayList<String>();
    ArrayList<String> car_array=new ArrayList<String>();
    ArrayList<RateCard_CityPojo> city_itemList;
    ArrayList<RateCard_CarPojo> car_itemList;
    ArrayList<RateCard_CardDisplayPojo> rate_itemList;
    ArrayList<RateCard_StdPojo> stdrate_itemList;

    private String SfirstKm="",SafterKm="";
    private String SfirstKm_fare="",SafterKm_fare="";
    RateCardAdapter adapter;
    RateCardStandardAdapter standardAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ratecard_page);
        initialize();

        //Start XMPP Chat Service
        ChatService.startUserAction(RateCard.this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.enter, R.anim.exit);
                finish();
            }
        });

        city_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                city_spinner.setFloatingLabelText(getResources().getString(R.string.ratecard_lable_selected_city_hint));
                cd = new ConnectionDetector(RateCard.this);
                isInternetPresent = cd.isConnectingToInternet();

                if (!city_spinner.getSelectedItem().toString().equalsIgnoreCase("Select City")) {
                    if (isInternetPresent) {
                        if (car_postrequest != null) {
                            car_postrequest.cancel();
                        }
                        Sselected_cityID = city_itemList.get(position).getCity_id();
                        postRequest_CarSelect(Iconstant.ratecard_select_cartype_url, city_itemList.get(position).getCity_id());
                    } else {
                        Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        carType_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                carType_spinner.setFloatingLabelText(getResources().getString(R.string.ratecard_lable_selected_car_hint));
                cd = new ConnectionDetector(RateCard.this);
                isInternetPresent = cd.isConnectingToInternet();

                if(!carType_spinner.getSelectedItem().toString().equalsIgnoreCase("Select Car Type"))
                {
                    if(isInternetPresent)
                    {
                        if(ratecard_postrequest!=null)
                        {
                            ratecard_postrequest.cancel();
                        }
                        rateCard_displayRequest(Iconstant.ratecard_display_url, Sselected_cityID, car_itemList.get(position).getCar_id());
                    }
                    else
                    {
                        Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        listview.setExpanded(true);
        standard_listview.setExpanded(true);
    }

    private void initialize() {

        session = new SessionManager(RateCard.this);
        cd = new ConnectionDetector(RateCard.this);
        isInternetPresent = cd.isConnectingToInternet();
        city_itemList=new ArrayList<RateCard_CityPojo>();
        car_itemList=new ArrayList<RateCard_CarPojo>();
        rate_itemList=new ArrayList<RateCard_CardDisplayPojo>();
        stdrate_itemList=new ArrayList<RateCard_StdPojo>();

        city_spinner=(MaterialSpinner)findViewById(R.id.ratecard_city_spinner);
        carType_spinner=(MaterialSpinner)findViewById(R.id.ratecard_cartype_spinner);
        standard_listview=(ExpandableHeightListView)findViewById(R.id.ratecard_standardRate_listView);
        listview=(ExpandableHeightListView)findViewById(R.id.ratecard_listView);
        back = (RelativeLayout) findViewById(R.id.ratecard_header_back_layout);
        rate_display_layout=(RelativeLayout)findViewById(R.id.ratecard_ratedisplay_layout);

        if(isInternetPresent)
        {
            postRequest_CitySelect(Iconstant.ratecard_select_city_url);
        }
        else
        {
            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
        }
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(RateCard.this);
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




    //-----------------------City Select Post Request-----------------
    private void postRequest_CitySelect(String Url)
    {
        dialog = new Dialog(RateCard.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title=(TextView)dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_processing));

        System.out.println("-------------CitySelect Url----------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------CitySelect Response----------------"+response);

                        String Sstatus = "";
                        try {

                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if(Sstatus.equalsIgnoreCase("1"))
                            {
                                JSONObject response_object= object.getJSONObject("response");
                                if(response_object.length()>0)
                                {
                                    JSONArray location_array = response_object.getJSONArray("locations");
                                    if(location_array.length()>0)
                                    {
                                        city_array.clear();
                                        city_itemList.clear();
                                        for (int i = 0; i <location_array.length(); i++)
                                        {
                                            JSONObject location_object = location_array.getJSONObject(i);
                                            RateCard_CityPojo city_pojo = new RateCard_CityPojo();
                                            city_pojo.setCity_id(location_object.getString("id"));
                                            city_pojo.setCity_name(location_object.getString("city"));

                                            city_array.add(location_object.getString("city"));

                                            city_itemList.add(city_pojo);
                                        }

                                        isCityAvailable=true;
                                    }
                                    else
                                    {
                                        isCityAvailable=false;
                                    }
                                }
                                else
                                {
                                    isCityAvailable=false;
                                }
                            }
                            else
                            {
                                isCityAvailable=false;
                            }


                            if (Sstatus.equalsIgnoreCase("1") && isCityAvailable)
                            {
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(RateCard.this,
                                        R.layout.ratecard_city_spinner_dropdown, city_array);
                                city_spinner.setAdapter(adapter);
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
                VolleyErrorResponse.volleyError(RateCard.this, error);
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
                jsonParams.put("", "");
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }


    //-----------------------Car Type Select Post Request-----------------
    private void postRequest_CarSelect(String Url, final String location_id)
    {
        dialog = new Dialog(RateCard.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title=(TextView)dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_processing));

        System.out.println("-------------CarSelect Url----------------" + Url);

        car_postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------CarSelect Response----------------"+response);

                        String Sstatus = "";
                        try {

                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if(Sstatus.equalsIgnoreCase("1"))
                            {
                                JSONObject response_object= object.getJSONObject("response");
                                if(response_object.length()>0)
                                {
                                    JSONArray location_array = response_object.getJSONArray("category");
                                    if(location_array.length()>0)
                                    {
                                        car_array.clear();
                                        car_itemList.clear();
                                        for (int i = 0; i <location_array.length(); i++)
                                        {
                                            JSONObject location_object = location_array.getJSONObject(i);
                                            RateCard_CarPojo car_pojo = new RateCard_CarPojo();
                                            car_pojo.setCar_id(location_object.getString("id"));
                                            car_pojo.setCat_type(location_object.getString("category"));

                                            car_array.add(location_object.getString("category"));
                                            car_itemList.add(car_pojo);
                                        }

                                        isCarAvailable=true;
                                    }
                                    else
                                    {
                                        isCarAvailable=false;
                                    }
                                }
                                else
                                {
                                    isCarAvailable=false;
                                }
                            }
                            else
                            {
                                isCarAvailable=false;
                            }



                            if (Sstatus.equalsIgnoreCase("1") && isCarAvailable)
                            {
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(RateCard.this,
                                        R.layout.ratecard_city_spinner_dropdown, car_array);
                                carType_spinner.setAdapter(adapter);
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
                VolleyErrorResponse.volleyError(RateCard.this, error);
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
                jsonParams.put("location_id",location_id);
                return jsonParams;
            }
        };
        car_postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        car_postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(car_postrequest);
    }



    //-----------------------Rate Card Display Post Request-----------------
    private void rateCard_displayRequest(String Url, final String location_id,final String category_id)
    {
        dialog = new Dialog(RateCard.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title=(TextView)dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_processing));

        System.out.println("-------------Rate Card Url----------------" + Url);

        ratecard_postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------Rate Card Response----------------"+response);

                        String Sstatus = "";
                        try {

                            JSONObject object = new JSONObject(response);
                            Sstatus = object.getString("status");
                            if(Sstatus.equalsIgnoreCase("1"))
                            {
                                JSONObject response_object= object.getJSONObject("response");
                                if(response_object.length()>0)
                                {
                                    JSONObject ratecard_object= response_object.getJSONObject("ratecard");
                                    if(ratecard_object.length()>0)
                                    {
                                        Currency currencycode = Currency.getInstance(getLocale(ratecard_object.getString("currency")));

                                        JSONArray standard_array = ratecard_object.getJSONArray("standard_rate");
                                        if(standard_array.length()>0)
                                        {
                                            stdrate_itemList.clear();
                                            for (int i = 0; i <standard_array.length(); i++) {
                                                JSONObject standard_object = standard_array.getJSONObject(i);
                                                RateCard_StdPojo stdrate_pojo = new RateCard_StdPojo();
                                                stdrate_pojo.setStdrate_title(standard_object.getString("title"));
                                                stdrate_pojo.setStdrate_sub_title(standard_object.getString("sub_title"));
                                                stdrate_pojo.setStdrate_fare(currencycode.getSymbol()+standard_object.getString("fare"));
                                                stdrate_pojo.setStdrate_currencySymbol(currencycode.getSymbol());

                                                stdrate_itemList.add(stdrate_pojo);
                                            }
                                        }

                                        JSONArray extra_array = ratecard_object.getJSONArray("extra_charges");
                                        if(extra_array.length()>0)
                                        {
                                            rate_itemList.clear();
                                            for (int j = 0; j <extra_array.length(); j++) {
                                                JSONObject extra_object = extra_array.getJSONObject(j);
                                                RateCard_CardDisplayPojo rate_pojo = new RateCard_CardDisplayPojo();
                                                rate_pojo.setRate_title(extra_object.getString("title"));
                                                rate_pojo.setRate_sub_title(extra_object.getString("sub_title"));
                                                rate_pojo.setRate_fare(extra_object.getString("fare"));
                                                rate_pojo.setRate_currencySymbol(currencycode.getSymbol());

                                                rate_itemList.add(rate_pojo);
                                            }
                                        }
                                        isCarAvailable=true;
                                    }
                                    else
                                    {
                                        isCarAvailable=false;
                                    }
                                }
                                else
                                {
                                    isCarAvailable=false;
                                }
                            }
                            else
                            {
                                isCarAvailable=false;
                            }


                            if (Sstatus.equalsIgnoreCase("1") && isCarAvailable)
                            {

                                rate_display_layout.setVisibility(View.VISIBLE);

                                if(stdrate_itemList.size()>0)
                                {
                                    standardAdapter=new RateCardStandardAdapter(RateCard.this,stdrate_itemList);
                                    standard_listview.setAdapter(standardAdapter);
                                }

                                if(rate_itemList.size()>0)
                                {
                                    adapter=new RateCardAdapter(RateCard.this,rate_itemList);
                                    listview.setAdapter(adapter);
                                }
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
                VolleyErrorResponse.volleyError(RateCard.this, error);
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
                jsonParams.put("location_id",location_id);
                jsonParams.put("category_id",category_id);
                return jsonParams;
            }
        };
        ratecard_postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        ratecard_postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(ratecard_postrequest);
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
}
