package android.updater;

import session.Session;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.sessionX.AndroidSession;

public class OnAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		System.out.println("Alarmed received!");
		// Acquire lock
		UpdaterService.acquireStaticLock(context);
		
		// Run service
		Intent i = new Intent(context, UpdaterService.class);
//		i.setData(intent.getData());
		context.startService(i);
		
		// Register new alarm
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (Session.session == null) {
			System.out.println("Session is null!");
		}
//		else if (((AndroidSession) Session.session).getTimeBetweenUpdates())
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ((AndroidSession) Session.session).getTimeBetweenUpdates(), PendingIntent.getBroadcast(context, 0, new Intent(context, OnAlarmReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT));
	}

}
