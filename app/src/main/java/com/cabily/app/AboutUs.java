package com.cabily.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import com.cabily.HockeyApp.ActivityHockeyApp;
import com.casperon.app.cabily.R;
import com.mylibrary.xmpp.ChatService;

/**
 * Created by Prem Kumar and Anitha on 10/12/2015.
 */
public class AboutUs extends ActivityHockeyApp
{
    private RelativeLayout back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutus);
        ChatService.startUserAction(AboutUs.this);
        back=(RelativeLayout)findViewById(R.id.aboutus_header_back_layout);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
    }

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
