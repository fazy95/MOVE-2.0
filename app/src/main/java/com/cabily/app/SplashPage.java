package com.cabily.app;

/**
 * Created by Prem Kumar on 10/1/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.cabily.HockeyApp.ActivityHockeyApp;
import com.mylibrary.gps.GPSTracker;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.mylibrary.xmpp.ChatService;

import java.util.HashMap;


public class SplashPage extends Activity
{
	// Splash screen timer
	private static int SPLASH_TIME_OUT = 2000;
	SessionManager session;
	Context context;
	
	String commonid;
	GPSTracker gps;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
	
		context=getApplicationContext();
		
		// Session class instance
		session = new SessionManager(getApplicationContext());
		gps=new GPSTracker(getApplicationContext());
		
		
		new Handler().postDelayed(new Runnable() 
		{
			@Override
			public void run() 
			{
				if (session.isLoggedIn()) 
				{
					//starting XMPP service
					ChatService.startUserAction(SplashPage.this);

					Intent i = new Intent(SplashPage.this, NavigationDrawer.class);
					startActivity(i);
					finish();
					overridePendingTransition(R.anim.enter,R.anim.exit);
				} 
				else
				{
					Intent i = new Intent(SplashPage.this, SingUpAndSignIn.class);
					startActivity(i);
					finish();
					overridePendingTransition(R.anim.enter,R.anim.exit);
				}
			}
		}, SPLASH_TIME_OUT);
	}
	
	
	
	/*private void EnableLocation()
	{
		final Dialog dialog = new Dialog(SplashPage.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.gps_dialog);
		
		TextView tv_no = (TextView) dialog.findViewById(R.id.gps_popup_text_no);
		TextView tv_yes = (TextView) dialog.findViewById(R.id.gps_popup_text_ok);

		tv_no.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				finish();
			}
		});
		
		tv_yes.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View arg0) {
				
				   String provider = Settings.Secure.getString(SplashPage.this.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
				    if(!provider.contains("gps")){ //if gps is disabled
				        final Intent poke = new Intent();
				        poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider"); 
				        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
				        poke.setData(Uri.parse("3")); 
				        SplashPage.this.sendBroadcast(poke);


				    }
				    
				    
				    WifiManager wifiManager = (WifiManager)SplashPage.this.getSystemService(Context.WIFI_SERVICE);
				    if(!wifiManager.isWifiEnabled())
				    {
				    	 wifiManager.setWifiEnabled(true);
				    }
				    
				    dialog.dismiss();
			}
		});
		
		dialog.show();
	}*/
}

