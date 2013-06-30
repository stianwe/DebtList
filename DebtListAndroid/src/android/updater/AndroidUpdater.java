package android.updater;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import session.Updater;

/**
 * @deprecated No longer supported!
 */
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
		// > v0.0.0.0_pre-release uses an intent service instead of a background thread.
//		super.updateRequester = new AndroidUpdateRequester(timeBetweenUpdates, shouldUpdateWithoutWifi, context);
//		(super.timer = new Timer()).schedule(updateRequester, timeBetweenUpdates, timeBetweenUpdates);
		
		Intent updateRequester = new Intent(context, UpdaterService.class);
		UpdaterServiceMessage msg = new UpdaterServiceMessage(shouldUpdateWithoutWifi);
//		updateRequester.setData(Uri.parse("DATA YOYO HOHO"));
		updateRequester.setData(Uri.parse(msg.toXML()));
		context.startActivity(updateRequester);
	}
}
