package android.sessionX;

import java.io.IOException;

import network.Constants;

import android.content.Context;
import android.network.AndroidConnection;

import session.Session;
import session.Updater;

public class AndroidSession extends Session {

	private AndroidConnection connection;
	private String sessionToken = null;
	private Updater updater;
	
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
	}
	
	public void startUpdater(Context context) {
		if(updater == null) {
			System.out.println("Creating new ANDROIDUpdater!!!!");
			updater = new AndroidUpdater(context);
		}
		updater.startUpdater(Constants.STANDARD_TIME_BETWEEN_UPDATES);
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
