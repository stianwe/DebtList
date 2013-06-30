package android.updater;

import java.io.IOException;

import requests.UpdateRequest;
import requests.xml.XMLSerializable;
import session.Session;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.utils.Tools;

public class UpdateServiceMessageReceiver extends BroadcastReceiver {

//	private UpdateServiceMessageReceiver() {}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//
		System.out.println("UpdateServiceMessageReceiver started!");
		Object o;
		try {
			o = XMLSerializable.toObject((String) intent.getExtras().get(UpdaterService.EXTENDED_DATA_RECEIVED_UDPATES));
			System.out.println("Update handler received a broadcast..");
			if(o instanceof UpdateRequest) {
				System.out.println("Processing updates..");
				UpdateRequest us = (UpdateRequest) o;
				System.out.println("Number of updates: " + us.size());
				for (int i = 0; i < us.size(); i++) {
					System.out.println("Processing update " + i);
					Session.session.processUpdate(us.get(i));
					Tools.createNotification(us.get(i), context);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
