package android.network;

import java.io.IOException;

import javax.net.ssl.SSLSocket;

import android.debtlistandroid.LoginActivity;
import android.os.Looper;
import android.sessionX.AndroidSession;
import android.utils.Tools;

import requests.LogInRequest;
import requests.Request;
import requests.xml.XMLSerializable;
import session.Session;

import network.ClientConnection;
import network.Constants;

/**
 * An implementation of Connection that closes after sending/receiving data
 */
public class AndroidConnection {

	private String host;
	private int port;
	
	public AndroidConnection(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	/**
	 * Runs the given runnable on a new separate thread
	 * @param run	The code to run
	 */
	public static void runOnSeparateThread(Runnable run) {
		new Thread(run).start();
	}
	
	private ClientConnection connect() throws IOException {
		ClientConnection con = new ClientConnection();
		System.out.println("Attempting to connect with an Android connection..");
		con.connect(host, port);
		System.out.println("Connected with Android connection!");
		if(!con.isConnected()) throw new IOException("Could not connect to " + host + ":" + port);
		return con;
	}
	
	private class ThreadX extends Thread {
		public String toBeReturned;
		public IOException ex = null;
		private boolean shouldSleep;
		
		public synchronized void setShouldSleep(boolean shouldSleep) {
			this.shouldSleep = shouldSleep;
		}
		
		public synchronized boolean shouldSleep() {
			return shouldSleep;
		}
		
	}
	
	/**
	 * Opens a connection to the host (specified with the constructor) and sends the given message.
	 * Will receive and return a response if specified
	 * 
	 * @param msg			The message to send
	 * @param shouldReceive	If we should receive and return a response
	 * @return				The received message, or null of not 
	 * @throws IOException	If the connection could not be established
	 */
	public String send(String msg, boolean shouldReceive) throws IOException {
		if(msg != null && shouldReceive)
			System.out.println("Sending and receiveing!");
		else if(msg != null && !shouldReceive)
			System.out.println("Sending without receiving!");
		else if(msg == null && shouldReceive)
			System.out.println("Not sending, but receiving!");
		else
			System.out.println("Not sending and not receiving. Should not happen!");
		final String message = msg;
		final boolean sr = shouldReceive;
		ThreadX x = new ThreadX() {
			
			private synchronized void wakeUp() {
				setShouldSleep(false);
				//					System.out.println("Trying to wake up sleeping thread..");
				notifyAll();
			}
			
			@Override
			public void run() {
				String xml = message;
				try {
					System.out.println("Connecting..");
					ClientConnection con = connect();
					if(xml != null) {
						XMLSerializable obj = XMLSerializable.toObject(xml);
						// Attach version
						if(obj instanceof Request) {
							Request r = (Request) obj;
//							r.setClientVersion(Constants.ANDROID_VERSION);
							r.setServerVersion(Constants.SERVER_VERSION);
							System.out.println("Attaching version: " + Constants.SERVER_VERSION);
						}
						// Attach username and password 
						if(((AndroidSession) Session.session).getPassword() != null) {
							// TODO: Should not need to parse back and forth when passing the objects instead of the strings
//							XMLSerializable o = XMLSerializable.toObject(xml);
							obj.setUserInformation(Session.session.getUser().getUsername(), ((AndroidSession) Session.session).getPassword());
//							xml = o.toXML();
//							con.send(new LogInRequest(Session.session.getUser().getUsername(), ((AndroidSession) Session.session).getPassword()).toXML());
						}
						// Attach the session token if present
						if(Session.session instanceof AndroidSession && ((AndroidSession) Session.session).getSessionToken() != null) {
							System.out.println("Attatching session token.");
							obj.setSessionToken(((AndroidSession) Session.session).getSessionToken());
						} else {
							// If not.. Request one!
							System.out.println("Requesting session token");
							obj.setSessionToken(Constants.SESSION_TOKEN_REQUEST);
						}
						xml = obj.toXML();
//						System.out.println("Sending message: " + xml);
						System.out.println("Sending message..");
						con.send(xml);
					} else {
						System.out.println("Won't send, only receive!");
					}
					if(sr) {
						System.out.println("Waiting for response..");
						toBeReturned = con.receive();
						System.out.println("Received: " + toBeReturned);
						// Check if our token has expired
						if(toBeReturned.equals(Constants.SESSION_EXPIRED)) {
							// Login again
							System.out.println("Attempting to log in again..");
							String username = Session.session.getUser().getUsername();
							String password = ((AndroidSession) Session.session).getPassword();
							Session.session.clear();
							Session.session.logIn(username, password);
							System.out.println("Attempt done.. Did it work?");
							// Re-send whatever we sent, and return the response
							toBeReturned = send(message, true);
							wakeUp();
							return;
						}
						XMLSerializable o = XMLSerializable.toObject(toBeReturned); 
						// Check if our version is outdated (but compatible)
						if(o instanceof Request) {
							Request r = (Request) o;
							System.out.println("Server version: " + r.getServerVersion());
							System.out.println("Client version: " + Constants.SERVER_VERSION);
							if(r.getServerVersion().isGreaterThan(Constants.SERVER_VERSION) && r.getServerVersion().isCompatible(Constants.SERVER_VERSION)) {
								System.out.println("Compatible but outdated version!");
								Session.session.setIsVersionOutdated(true);
//								LoginActivity.view.post(new Runnable() {
//									public void run() {
//										Tools.displayOutdatedVersionDialog(LoginActivity.context);
//									}
//								});
							} else {
								System.out.println("Version not outdated (might be incompatible though!)");
							}
						}
						// Update our session token
						String token = o.getSessionToken();
						if(token == null) {
							System.out.println("Something wrong happened! No session token was received from the server.");
						} else if(! (Session.session instanceof AndroidSession)) {
							System.out.println("Something is wrong! You should definitely be using an AndroidSession..");
						} else {
							AndroidSession s = (AndroidSession) Session.session;
							if(s.getSessionToken() == null) {
								System.out.println("Received token: " + token);
							} else {
								System.out.println("Received new token without no reason.. Sure.. (Might happen if we sent a CreateUserRequest)");
							}
							s.setSessionToken(token);
						}
						wakeUp();
//						System.out.println("Response received: " + toBeReturned);
						System.out.println("Received response!");
					} else {
//						System.out.println("Should not receive response!");
					}
					con.close();
				} catch (IOException e) {
					ex = e;
					System.out.println("Caught exception!");
					wakeUp();
					// Can we just throw it here?
//					throw e;
				}
			}
		};
		x.shouldSleep = shouldReceive;
		x.start();
		if(x.shouldSleep()) {
			try {
//				System.out.println("Going to sleep..");
				synchronized (x) {
					x.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
//				System.out.println("Woke up!" + e);
			}
		} else {
//			System.out.println("Won't sleep!");
		}
		if(x.ex != null) {
			System.out.println("Exception: " + x.ex);
			throw x.ex;
		} else {
//			System.out.println("No exception!");
		}
//		System.out.println("Stopped waiting!");
//		System.out.println("Returning: " + x.toBeReturned);
		return x.toBeReturned;
	}
	
	public String receive() throws IOException{
		return send(null, true);
//		throw new RuntimeException("AndroidConnection.receive is not yet supported!");
//		ThreadX x = new ThreadX() {
//			@Override
//			public void run() {
//				try {
//					ClientConnection con = connect();
//					toBeReturned = con.receive();
//					con.close();
//				} catch(IOException e) {
//					ex = e;
//				}
//			}
//		};
//		x.start();
//		if(x.ex != null) {
//			throw x.ex;
//		}
//		return x.toBeReturned;
	}
}
