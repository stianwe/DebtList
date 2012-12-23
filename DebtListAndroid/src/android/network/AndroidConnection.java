package android.network;

import java.io.IOException;

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
	
	private ClientConnection connect() throws IOException {
		ClientConnection con = new ClientConnection();
		con.connect(host, port);
		if(!con.isConnected()) throw new IOException("Could not connect to " + host + ":" + port);
		return con;
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
		ClientConnection con = connect();
		con.send(msg);
		String rec = null;
		if(shouldReceive) {
			rec = con.receive();
		}
		con.close();
		return rec;
	}
	
	public String receive() throws IOException{
		ClientConnection con = connect();
		String rec = con.receive();
		con.close();
		return rec;
	}
}
