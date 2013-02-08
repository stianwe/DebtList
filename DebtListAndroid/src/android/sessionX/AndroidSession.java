package android.sessionX;

import java.io.IOException;

import android.network.AndroidConnection;

import session.Session;

public class AndroidSession extends Session {

	private AndroidConnection connection;
	private String sessionToken = null;
	
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
