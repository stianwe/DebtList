package android.network;

import java.io.IOException;

import javax.net.ssl.SSLSocket;

import android.sessionX.AndroidSession;

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
			@Override
			public void run() {
				String xml = message;
				try {
					System.out.println("Connecting..");
					ClientConnection con = connect();
					if(xml != null) {
						// Attach the session token if present
						XMLSerializable obj = XMLSerializable.toObject(xml);
						if(Session.session instanceof AndroidSession && ((AndroidSession) Session.session).getSessionToken() != null) {
							System.out.println("Attatching session token.");
							obj.setSessionToken(((AndroidSession) Session.session).getSessionToken());
						} else {
							// If not.. Request one!
							System.out.println("Requesting session token");
							obj.setSessionToken(Constants.SESSION_TOKEN_REQUEST);
						}
						xml = obj.toXML();
						System.out.println("Sending message: " + xml);
						con.send(xml);
					} else {
						System.out.println("Won't send, only receive!");
					}
					if(sr) {
						System.out.println("Waiting for response..");
						toBeReturned = con.receive();
						System.out.println("Received: " + toBeReturned);
						// Update our session token
						String token = XMLSerializable.toObject(toBeReturned).getSessionToken();
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
						synchronized (this) {
							setShouldSleep(false);
							System.out.println("Trying to wake up sleeping thread..");
							notifyAll();
						}
						System.out.println("Response received: " + toBeReturned);
					} else {
						System.out.println("Should not receive response!");
					}
					con.close();
				} catch (IOException e) {
					ex = e;
					System.out.println("Caught exception!");
					synchronized (this) {
						setShouldSleep(false);
						System.out.println("Trying to wake up sleeping thread..");
						notifyAll();
					}
					// Can we just throw it here?
//					throw e;
				}
			}
		};
		x.shouldSleep = shouldReceive;
		x.start();
		if(x.shouldSleep()) {
			try {
				System.out.println("Going to sleep..");
				synchronized (x) {
					x.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println("Woke up!" + e);
			}
		} else {
			System.out.println("Won't sleep!");
		}
		if(x.ex != null) {
			System.out.println("Exception: " + x.ex);
			throw x.ex;
		} else {
			System.out.println("No exception!");
		}
		System.out.println("Stopped waiting!");
		System.out.println("Returning: " + x.toBeReturned);
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
