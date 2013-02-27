package android.sessionX;

import java.util.Timer;

import android.content.Context;
import session.Updater;

public class AndroidUpdater extends Updater {

	private Context context;
	
	public AndroidUpdater(Context context) {
		System.out.println("Creating AndroidUpdater..");
		this.context = context;
	}
	
	@Override
	public void startUpdater(long timeBetweenUpdates) {
		super.updateRequester = new AndroidUpdateRequester(timeBetweenUpdates, context);
		(super.timer = new Timer()).schedule(updateRequester, timeBetweenUpdates, timeBetweenUpdates);
	}
}
