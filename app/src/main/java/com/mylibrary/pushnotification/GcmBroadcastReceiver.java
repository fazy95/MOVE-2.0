package com.mylibrary.pushnotification;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * @author Prem Kumar
 *
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{

		System.out.println("-------------received push notification--------------"+intent);

		ComponentName comp = new ComponentName(context.getPackageName(), GCMNotificationIntentService.class.getName());
		startWakefulService(context, (intent.setComponent(comp)));
		//setResultCode(Activity.RESULT_OK);
	}
}
