package com.cabily.app;

/**
 * Created by Prem Kumar on 10/1/2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.cabily.HockeyApp.ActivityHockeyApp;
import com.casperon.app.cabily.R;

public class SingUpAndSignIn extends ActivityHockeyApp
{
	private Button signin,register;
	public static SingUpAndSignIn activty;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signin_and_signup);
		activty=this;
		
		initialize();
		
		signin.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				Intent i = new Intent(SingUpAndSignIn.this, LoginPage.class);
				startActivity(i);
				overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
			}
		});
		
		register.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				Intent i = new Intent(SingUpAndSignIn.this, RegisterPage.class);
				startActivity(i);
				overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
			}
		});
	}

	private void initialize()
	{
		signin=(Button)findViewById(R.id.signin_main_button);
		register=(Button)findViewById(R.id.register_main_button);

		signin.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf"));
		register.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf"));
	}

}
