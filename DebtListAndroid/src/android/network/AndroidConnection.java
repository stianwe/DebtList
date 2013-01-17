package android.network;

import java.io.IOException;

import javax.net.ssl.SSLSocket;

import android.sessionX.AndroidSession;

import requests.xml.XMLSerializable;
import session.Session;

import network.ClientConnection;

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
		con.connect(host, port);
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
						if(Session.session instanceof AndroidSession && ((AndroidSession) Session.session).getSessionToken() != null) {
							System.out.println("Attatching session token.");
							XMLSerializable obj = XMLSerializable.toObject(xml);
							obj.setSessionToken(((AndroidSession) Session.session).getSessionToken());
							xml = obj.toXML();
						}
						System.out.println("Sending message: " + xml);
						con.send(xml);
					} else {
						System.out.println("Won't send, only receive!");
					}
					if(sr) {
						System.out.println("Waiting for response..");
						toBeReturned = con.receive();
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
