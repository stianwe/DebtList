package android.sessionX;

import java.util.List;

import android.content.Context;
import android.debtlistandroid.DebtViewActivity;
import android.utils.Tools;

import requests.xml.XMLSerializable;
import session.UpdateRequester;

public class AndroidUpdateRequester extends UpdateRequester {

	private Context context;
	
	public AndroidUpdateRequester(long timeBetweenUpdates, Context context) {
		super(timeBetweenUpdates);
		this.context = context;
	}

	@Override
	public void run() {
		// Only run updates after the given time interval (mainly so that we will not poll for updates
		// while the user is sending/receiving something).
		//				if(System.currentTimeMillis() - lastUpdate >= timeBetweenUpdates) {
		
		// Display dummy notification
		Tools.createNotification(context, "UPDATE", "DebtList has updated", DebtViewActivity.class, DebtViewActivity.class);
		
		List<XMLSerializable> updates = update();
		System.out.println("ANDROID AHAHHAAHAHAHHAHAASJDKLAJSDKLJAKSDJ RUNAR!");
		for (XMLSerializable u : updates) {
			System.out.println("DISPLAY NOTIFICATION!!!!!!");
		}
		super.updateLastUpdate();
		//				}
	}

}
