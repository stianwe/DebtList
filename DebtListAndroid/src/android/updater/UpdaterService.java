package android.updater;

import java.io.IOException;

import requests.xml.XMLSerializable;
import session.UpdateRequester;
import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class UpdaterService extends IntentService {

	public static final String WORKER_THREAD_NAME = "debtlist.UpdaterServiceWorkerThread";
	public static final String UPDATE_ACTION = "debtlist.updateAction";
	public static final String EXTENDED_DATA_RECEIVED_UDPATES = "debtlist.receivedUpdates";
	
	private Context context;
	
	public UpdaterService(Context context) {
		super(WORKER_THREAD_NAME);
		this.context = context;
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		System.out.println("UpdaterService.onHandleIntent() started!");
		String dataString = intent.getDataString();
		UpdaterServiceMessage msg;
		try {
			msg = (UpdaterServiceMessage) XMLSerializable.toObject(dataString);
			// Fetch updates if wifi is connected or user has specified to fetch updates without wifi
			if(!msg.shouldUpdateWithoutWifi() && 
					!((ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
				// Don't poll for updates
				System.out.println("Not polling for updates since wifi is off, and updates without wifi is off. :(");
				return;
			}
			String updates;
			try {
				updates = UpdateRequester.fetchUpdates().toXML();
				// Notify activities about updates (if we received any)
				Intent localIntent = new Intent(UPDATE_ACTION)
				.putExtra(EXTENDED_DATA_RECEIVED_UDPATES, updates);
			} catch (IOException e) {
				System.out.println("Could not fetch updates: ");
				e.printStackTrace();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("UpdaterService.onHanleIntent() stopped!");
	}

}
