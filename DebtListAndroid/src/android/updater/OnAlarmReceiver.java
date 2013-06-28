package android.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class OnAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// FIXME!!! LOCK!!!
		
		// Run service
		context.startService(new Intent(context, UpdaterService.class));
	
		// TODO: Register new alarm
	}

}
