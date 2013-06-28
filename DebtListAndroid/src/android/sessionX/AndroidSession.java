package android.sessionX;

import java.io.IOException;

import network.Constants;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.network.AndroidConnection;
import android.support.v4.content.LocalBroadcastManager;
import android.updater.AndroidUpdater;
import android.updater.OnAlarmReceiver;
import android.updater.UpdateServiceMessageReceiver;

import requests.LogInRequestStatus;
import session.Session;
import session.Updater;

public class AndroidSession extends Session {

	private AndroidConnection connection;
	private String sessionToken = null;
	private Updater updater;
	private String password;
	
	private boolean updaterIsRunning = false;
	
	public AndroidSession() {
		init();
	}
	
	/**
	 * Clears the session by reseting the user, and by removing the session token.
	 */
	@Override
	public void clear() {
		super.clear();
		sessionToken = null;
		password = null;
	}
	
	@Override
	public LogInRequestStatus logIn(String username, String password) {
		LogInRequestStatus s = super.logIn(username, password);
		if(s == LogInRequestStatus.ACCEPTED) {
			this.password = password;
		}
		return s;
	}

	@Override
	public LogInRequestStatus logIn(String username, String password, String activationKey) {
		LogInRequestStatus s = super.logIn(username, password, activationKey);
		if(s == LogInRequestStatus.ACCEPTED) {
			this.password = password;
		}
		return s;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void startUpdater(Context context, long timeBetweenUpdates, boolean shouldUpdateWithoutWifi) {
		// Stop old updater if it exists
		/*if(updater != null) {
			updater.stopUpdater();
		}
//		if(updater == null) {
			System.out.println("Creating new ANDROIDUpdater!!!!");
			updater = new AndroidUpdater(context, shouldUpdateWithoutWifi);
			updater.startUpdater(timeBetweenUpdates);
//		}
//		updater.startUpdater(Constants.STANDARD_TIME_BETWEEN_UPDATES); */
		
		// Only start updater if it is not already running
		if(!updaterIsRunning) {
			UpdateServiceMessageReceiver receiver = new UpdateServiceMessageReceiver();
			LocalBroadcastManager.getInstance(context).registerReceiver(receiver, null);
			
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, timeBetweenUpdates, PendingIntent.getBroadcast(context, 0, new Intent(context, OnAlarmReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT));
			updaterIsRunning = true;
		}
	}
	
	public void stopUpdater() {
		if(updater != null) {
			updater.stopUpdater();
		}
	}
	
	@Override
	public void send(String msg)  {
		try {
			connection.send(msg, false);
		} catch (IOException e) {
			// TODO: Do nothing?
		}
	}

	@Override
	public void connect(String host, int port) {
		connection = new AndroidConnection(host, port);
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public String sendAndReceive(String msg) throws IOException {
		System.out.println("Sending over an Android connection.");
		return connection.send(msg, true);
	}

	@Override
	public void init() {
		session = this;
	}

	@Override
	public String receive() throws IOException {
		return connection.receive();
	}
	
	public void setSessionToken(String token) {
		this.sessionToken = token;
	}
	
	public String getSessionToken() {
		return sessionToken;
	}
}
