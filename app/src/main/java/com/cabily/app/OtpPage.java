package com.cabily.app;

/**
 * Created by Prem Kumar on 10/1/2015.
 */

import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.cabily.HockeyApp.ActivityHockeyApp;
import com.cabily.iconstant.Iconstant;
import com.mylibrary.volley.AppController;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.casperon.app.cabily.R;
import com.mylibrary.volley.VolleyErrorResponse;
import com.mylibrary.xmpp.ChatService;

import me.drakeet.materialdialog.MaterialDialog;


public class OtpPage extends ActivityHockeyApp
{
	private Context context;
	private	Boolean isInternetPresent = false;
	private	ConnectionDetector cd;
	private SessionManager session;
	
	private RelativeLayout back;
	private EditText Eotp;
	private Button send;
	
	StringRequest postrequest;
	Dialog dialog;
	
	private String Susername="",Semail="",Spassword="",Sphone="",ScountryCode="",SreferalCode="",SgcmId="";
	private String Sotp_Status="",Sotp="";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.otp_page);
		context = getApplicationContext();
		initialize();
		
		back.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				// close keyboard
				InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(back.getWindowToken(), 0);	
				
				onBackPressed();
				overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
				finish();	
			}
		});
		
		send.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
			
				if(Eotp.getText().toString().length()==0)
				{
					erroredit(Eotp,getResources().getString(R.string.otp_label_alert_otp));
				}
				else if(!Sotp.equals(Eotp.getText().toString()))
				{
					erroredit(Eotp,getResources().getString(R.string.otp_label_alert_invalid));
				}
				else
				{
					cd = new ConnectionDetector(OtpPage.this);
					isInternetPresent = cd.isConnectingToInternet();

					if(isInternetPresent)
					{
						PostRequest(Iconstant.register_otp_url);
					}
					else
					{
						Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
					}
				}
			}
		});
		
		
		Eotp.setOnEditorActionListener(new OnEditorActionListener() {
	        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	            if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
	            	 InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	                 in.hideSoftInputFromWindow(Eotp.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
	            }
	            return false;
	        }
	    });
	}

	private void initialize() 
	{
		session=new SessionManager(OtpPage.this);
		cd = new ConnectionDetector(OtpPage.this);
		isInternetPresent = cd.isConnectingToInternet();
		
		back=(RelativeLayout)findViewById(R.id.otp_header_back_layout);
		Eotp=(EditText)findViewById(R.id.otp_password_editText);
		send=(Button)findViewById(R.id.otp_submit_button);
		
		Eotp.addTextChangedListener(EditorWatcher);
		
		Intent intent=getIntent();
		Susername=intent.getStringExtra("UserName");
		Semail=intent.getStringExtra("Email");
		Spassword=intent.getStringExtra("Password");
		Sphone=intent.getStringExtra("Phone");
		ScountryCode=intent.getStringExtra("CountryCode");
		SreferalCode=intent.getStringExtra("ReferalCode");
		SgcmId=intent.getStringExtra("GcmID");
		Sotp_Status=intent.getStringExtra("Otp_Status");
		Sotp=intent.getStringExtra("Otp");
		
		if(Sotp_Status.equalsIgnoreCase("development"))
		{
			Eotp.setText(Sotp);
		}
		else
		{
			Eotp.setText("");
		}
	}

	//--------------Alert Method-----------
	private void Alert(String title, String alert) {
		final MaterialDialog dialog = new MaterialDialog(OtpPage.this);
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

	//----------------------Code for TextWatcher-------------------------
			private final TextWatcher EditorWatcher = new TextWatcher()
			{
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{
				}
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{
				}
				@Override
				public void afterTextChanged(Editable s)
				{
					//clear error symbol after entering text
					if (Eotp.getText().length() > 0)
					{
						Eotp.setError(null);
					}
				}
			};	
		
		//--------------------Code to set error for EditText-----------------------
		private void erroredit(EditText editname,String msg)
		{
			Animation shake = AnimationUtils.loadAnimation(OtpPage.this, R.anim.shake);
			editname.startAnimation(shake);
			
			ForegroundColorSpan fgcspan = new ForegroundColorSpan(Color.parseColor("#CC0000"));
			SpannableStringBuilder ssbuilder = new SpannableStringBuilder(msg);
			ssbuilder.setSpan(fgcspan, 0, msg.length(), 0);
			editname.setError(ssbuilder);
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
		
		//-----------------Move Back on pressed phone back button------------------		
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) 
		{
			if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {
				
				// close keyboard
				InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(back.getWindowToken(), 0);	
				
				OtpPage.this.finish();
				OtpPage.this.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
				return true;
			}
			return false;
		}	
		
		
		// -------------------------code for Login Post Request----------------------------------			

		private void PostRequest(String Url) 
		{
			
			dialog = new Dialog(OtpPage.this);
	        dialog.getWindow();
	        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);   
	        dialog.setContentView(R.layout.custom_loading);
	        dialog.setCanceledOnTouchOutside(false);
	        dialog.show();
	        
	         TextView dialog_title=(TextView)dialog.findViewById(R.id.custom_loading_textview);
	         dialog_title.setText(getResources().getString(R.string.action_otp));
	        
			    System.out.println("--------------Otp url-------------------"+Url);
			
				 postrequest = new StringRequest(Request.Method.POST, Url, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {

						System.out.println("--------------Otp reponse-------------------"+response);
		            	
						
						String Sstatus="",Smessage="",Suser_image="",Suser_id="",Suser_name="",
								Semail="",Scountry_code="",SphoneNo="",Sreferal_code="",Scategory="",
								SsecretKey="",SwalletAmount="",ScurrencyCode="";
						Currency currencycode = null;
							   
						try {
							
							JSONObject object=new JSONObject(response);
							
							Sstatus = object.getString("status");
							Smessage = object.getString("message");
							
							if(Sstatus.equalsIgnoreCase("1"))
							{
								Suser_image = object.getString("user_image");
								Suser_id = object.getString("user_id");
								Suser_name = object.getString("user_name");
								Semail= object.getString("email");
								Scountry_code= object.getString("country_code");
								SphoneNo= object.getString("phone_number");
								Sreferal_code= object.getString("referal_code");
								Scategory= object.getString("category");
								SsecretKey = object.getString("sec_key");
								SwalletAmount = object.getString("wallet_amount");
								ScurrencyCode=object.getString("currency");
								currencycode = Currency.getInstance(getLocale(ScurrencyCode));
							}
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
						if(Sstatus.equalsIgnoreCase("1"))
						{
						    SingUpAndSignIn.activty.finish();
						    
							session.createLoginSession(Semail ,Suser_id, Suser_name, Suser_image,Scountry_code,SphoneNo,Sreferal_code,Scategory);
							session.createWalletAmount(currencycode.getSymbol()+SwalletAmount);
							session.setXmppKey(Suser_id, SsecretKey);

							//starting XMPP service
							ChatService.startUserAction(OtpPage.this);

							Intent intent = new Intent(context, NavigationDrawer.class);
							startActivity(intent);
							overridePendingTransition(R.anim.enter, R.anim.exit);
							finish();
						}
						else
						{
							final MaterialDialog alertDialog= new MaterialDialog(OtpPage.this);
							alertDialog.setTitle("Error");
							alertDialog
									.setMessage(Smessage)
									.setCanceledOnTouchOutside(false)
									.setPositiveButton(
						                    "OK", new OnClickListener() {
						                        @Override
						                        public void onClick(View v) {
						                        	alertDialog.dismiss();
						                        }
						                    }
						                ).show();
						}
						
						// close keyboard
						InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						mgr.hideSoftInputFromWindow(Eotp.getWindowToken(), 0);	
						
						dialog.dismiss();
						
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						
						dialog.dismiss();
						VolleyErrorResponse.volleyError(OtpPage.this, error);
					}
				})  {

					 @Override
					 public Map<String, String> getHeaders() throws AuthFailureError {
						 Map<String, String> headers = new HashMap<String, String>();
						 headers.put("User-agent",Iconstant.cabily_userAgent);
						 return headers;
					 }

					@Override
					protected Map<String, String> getParams() throws AuthFailureError {
						Map<String, String> jsonParams = new HashMap<String, String>();
						jsonParams.put("user_name", Susername);
						jsonParams.put("email", Semail);
						jsonParams.put("password", Spassword);
						jsonParams.put("phone_number", Sphone);
						jsonParams.put("country_code", ScountryCode);
						jsonParams.put("referal_code", SreferalCode);
						jsonParams.put("gcm_id", SgcmId);
						return jsonParams;
					}
				};
				//to avoid repeat request Multiple Time
				DefaultRetryPolicy  retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
				postrequest.setRetryPolicy(retryPolicy);
			 	postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
				AppController.getInstance().addToRequestQueue(postrequest);
		}
}
