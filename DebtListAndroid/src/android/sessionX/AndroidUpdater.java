package android.sessionX;

import java.util.Timer;

import android.content.Context;
import session.Updater;

public class AndroidUpdater extends Updater {

	private Context context;
	private boolean shouldUpdateWithoutWifi;
	
	public AndroidUpdater(Context context, boolean shouldUpdateWithoutWifi) {
		System.out.println("Creating AndroidUpdater..");
		this.context = context;
		this.shouldUpdateWithoutWifi = shouldUpdateWithoutWifi;
	}
	
	@Override
	public void startUpdater(long timeBetweenUpdates) {
		if(timeBetweenUpdates == 0) {
			// Don't update
			return;
		}
		super.updateRequester = new AndroidUpdateRequester(timeBetweenUpdates, shouldUpdateWithoutWifi, context);
		(super.timer = new Timer()).schedule(updateRequester, timeBetweenUpdates, timeBetweenUpdates);
	}
}
