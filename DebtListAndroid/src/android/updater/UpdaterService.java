package android.updater;

import java.io.IOException;

import requests.xml.XMLSerializable;
import session.Session;
import session.UpdateRequester;
import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.PowerManager;
import android.sessionX.AndroidSession;
import android.support.v4.content.LocalBroadcastManager;

public class UpdaterService extends IntentService {

	public static final String WORKER_THREAD_NAME = "debtlist.UpdaterServiceWorkerThread";
	public static final String UPDATE_ACTION = "debtlist.updateAction";
	public static final String EXTENDED_DATA_RECEIVED_UDPATES = "debtlist.receivedUpdates";

	private static Context context;
	public static final String LOCK_NAME_STATIC = "debtlist.staticLock";
	private static PowerManager.WakeLock lockStatic = null;
	
	public static boolean shouldUpdateWithoutWifi;


	public UpdaterService() {
		super(WORKER_THREAD_NAME);
	}

	public static void acquireStaticLock(Context context) {
		UpdaterService.context = context;
		getLock(context).acquire();
	}

	synchronized private static PowerManager.WakeLock getLock(Context context) {
		if (lockStatic == null) {
			PowerManager powManager = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			lockStatic = powManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					LOCK_NAME_STATIC);
			lockStatic.setReferenceCounted(true);
		}
		return (lockStatic);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		System.out.println("UpdaterService.onHandleIntent() started!");
		if(Session.session == null) {
			System.out.println("Session is null! App has probably been killed.\nTerminating update.");
			return;
		}
		try {
//			String dataString = intent.getData().toString();
			UpdaterServiceMessage msg;
//			try {
//				msg = (UpdaterServiceMessage) XMLSerializable.toObject(dataString);
				// Fetch updates if wifi is connected or user has specified to fetch updates without wifi
//				if(!msg.shouldUpdateWithoutWifi() &&
				if(!shouldUpdateWithoutWifi && 
						!((ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
					// Don't poll for updates
					System.out.println("Not polling for updates since wifi is off, and updates without wifi is off. :(");
					return;
				}
				String updates;
				try {
					System.out.println("Fetching updates..");
					updates = UpdateRequester.fetchUpdates().toXML();
					// Notify activities about updates (if we received any)
					Intent localIntent = new Intent(UPDATE_ACTION)
					.putExtra(EXTENDED_DATA_RECEIVED_UDPATES, updates);
					localIntent.setClass(context, UpdateServiceMessageReceiver.class);
					System.out.println("Attempting to notify UpdateServiceMessageReceiver..");
					LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
//					((AndroidSession) Session.session).getBroadcastManager().sendBroadcast(localIntent);
//					context.sendBroadcast(localIntent);
				} catch (IOException e) {
					System.out.println("Could not fetch updates: ");
					e.printStackTrace();
				}
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
		} finally {
			getLock(this).release();
		}
		System.out.println("UpdaterService.onHandleIntent() stopped!");
	}

}
