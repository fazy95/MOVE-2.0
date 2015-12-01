package com.cabily.app;

/**
 * Created by Prem Kumar on 10/1/2015.
 */

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cabily.HockeyApp.ActionBarActivityHockeyApp;
import com.cabily.adapter.HomeMenuListAdapter;
import com.cabily.fragment.Fragment_HomePage;
import com.cabily.utils.ConnectionDetector;
import com.casperon.app.cabily.R;

import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;

public class NavigationDrawer extends ActionBarActivityHockeyApp {
    ActionBarDrawerToggle actionBarDrawerToggle;
    static DrawerLayout drawerLayout;
    private static RelativeLayout mDrawer;
    private Context context;
    private ListView mDrawerList;

    private static HomeMenuListAdapter mMenuAdapter;
    private String[] title;
    private int[] icon;

    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;

    Fragment bookmyride = new Fragment_HomePage();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);
        context = getApplicationContext();

        drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer);
        mDrawer = (RelativeLayout) findViewById(R.id.drawer);
        mDrawerList = (ListView) findViewById(R.id.drawer_listview);

        /*MaterialRippleLayout.on(mDrawerList)
                .rippleColor(R.color.ripple_blue_color)
                .create();*/

        if (savedInstanceState == null) {
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.replace(R.id.content_frame, new Fragment_HomePage());
            tx.commit();
        }

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();


        title = new String[]{"username", getResources().getString(R.string.navigation_label_bookmyride),
                getResources().getString(R.string.navigation_label_myride),
                getResources().getString(R.string.navigation_label_money),
                getResources().getString(R.string.navigation_label_invite),
                getResources().getString(R.string.navigation_label_ratecard),
                getResources().getString(R.string.navigation_label_emergency),
                getResources().getString(R.string.navigation_label_report_issue),
                getResources().getString(R.string.navigation_label_aboutus),
        };

        icon = new int[]{R.drawable.no_profile_image_avatar_icon, R.drawable.book_my_ride,
                R.drawable.myride, R.drawable.cabily_money,
                R.drawable.invite_and_earn, R.drawable.rate_card, R.drawable.emergency_contact,R.drawable.report_issue_icon, R.drawable.aboutus_icon};

        mMenuAdapter = new HomeMenuListAdapter(context, title, icon);
        mDrawerList.setAdapter(mMenuAdapter);
        mMenuAdapter.notifyDataSetChanged();


        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                cd = new ConnectionDetector(NavigationDrawer.this);
                isInternetPresent = cd.isConnectingToInternet();

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                switch (position) {

                    case 0:
                        Intent intent = new Intent(NavigationDrawer.this, ProfilePage.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                        break;
                    case 1:
                        ft.replace(R.id.content_frame, bookmyride);
                        break;
                    case 2:
                        if(isInternetPresent)
                        {
                            Intent myRide_intent = new Intent(NavigationDrawer.this, MyRides.class);
                            startActivity(myRide_intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                        }
                        else
                        {
                            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                        }
                        break;
                    case 3:
                        if(isInternetPresent)
                        {
                            Intent emergency_intent = new Intent(NavigationDrawer.this, CabilyMoney.class);
                            startActivity(emergency_intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                        }
                        else
                        {
                            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                        }
                        break;
                    case 4:
                        if(isInternetPresent)
                        {
                            Intent invite_intent = new Intent(NavigationDrawer.this, InviteAndEarn.class);
                            startActivity(invite_intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                        }
                        else
                        {
                            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                        }
                        break;
                    case 5:
                        if(isInternetPresent)
                        {
                            Intent emergency_intent = new Intent(NavigationDrawer.this, RateCard.class);
                            startActivity(emergency_intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                        }
                        else
                        {
                            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                        }
                        break;
                    case 6:
                        if(isInternetPresent)
                        {
                            Intent emergency_intent = new Intent(NavigationDrawer.this, EmergencyContact.class);
                            startActivity(emergency_intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                        }
                        else
                        {
                            Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                        }
                        break;
                    case 7:
                        sendEmail();
                        break;
                    case 8:
                        Intent about_intent = new Intent(NavigationDrawer.this, AboutUs.class);
                        startActivity(about_intent);
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                        break;
                }
                ft.commit();
                mDrawerList.setItemChecked(position, true);
                drawerLayout.closeDrawer(mDrawer);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
            /*int id = item.getItemId();

	        //noinspection SimplifiableIfStatement
	        if (id == R.id.action_settings) {
	            return true;
	        }*/

        return super.onOptionsItemSelected(item);
    }

    public static void openDrawer() {
        drawerLayout.openDrawer(mDrawer);
    }

    public static void disableSwipeDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public static void enableSwipeDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }


    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(NavigationDrawer.this);
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

    //----------Method to Send Email--------
    protected void sendEmail()
    {
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
        }
        catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(NavigationDrawer.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }


    public static void navigationNotifyChange() {
        mMenuAdapter.notifyDataSetChanged();
    }

   /* public void shareToGMail()
    {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"info@zoplay.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        final PackageManager pm = NavigationDrawer.this.getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
        ResolveInfo best = null;
        for(final ResolveInfo info : matches)
            if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail"))
                best = info;
        if (best != null)
            emailIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
        NavigationDrawer.this.startActivity(emailIntent);
    }*/
}
