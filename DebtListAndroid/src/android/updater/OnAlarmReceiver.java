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
		// Register new alarm if updates are not disabled
		if (Session.session != null && 
				((AndroidSession) Session.session).isUpdaterRunning() && 
				((AndroidSession) Session.session).getTimeBetweenUpdates() > 0) {
			// Acquire lock
			UpdaterService.acquireStaticLock(context);
			
			// Run service
			Intent i = new Intent(context, UpdaterService.class);
			context.startService(i);
			
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, 
					System.currentTimeMillis() + ((AndroidSession) Session.session).getTimeBetweenUpdates(), 
					PendingIntent.getBroadcast(
							context, 0, new Intent(context, OnAlarmReceiver.class), 
							PendingIntent.FLAG_UPDATE_CURRENT));
		} else {
			System.out.println("Not running update, since it is turned off!");
		}
	}

}
