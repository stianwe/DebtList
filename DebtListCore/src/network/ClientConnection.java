package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *	An insecure client connection using the ordinary Java Socket
 */
public class ClientConnection {

	private Socket connection;
	private BufferedReader reader;
	private PrintWriter writer;
	private boolean isConnected;
	
	/**
	 * Connect to the given host at the given port, and create the reader and the writer from the socket connection
	 * @param host	The host
	 * @param port	The port
	 */
	public void connect(String host, int port) throws IOException {
		isConnected = false;
		try {
			connection = createSocket(host, port);
			// Set timeout
			connection.setSoTimeout(Constants.STANDARD_SOCKET_RECEIVE_TIMEOUT);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			writer = new PrintWriter(connection.getOutputStream(), true);
			isConnected = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("FAILED TO CONNECT!!!!!!!!!!!!!!!!!!!!");
			try {
				connection.close();
			} catch (Exception ex) {}
			System.out.println("Throwing exception!");
			throw e;
		}
	}
	
	public Socket createSocket(String host, int port) throws IOException {
		Socket s = new Socket();
		// Connect and set the timeout
		System.out.println("Waiting up to 10 sec..");
		s.connect(new InetSocketAddress(host, port), Constants.STANDARD_SOCKET_CONNECT_TIMEOUT);
		System.out.println("Connected!! :)");
		return s;
	}
	
	/**
	 * Checks if this ClientConnection is connected to something
	 * @return	True if connected, false if not
	 */
	public boolean isConnected() {
//		return connection != null && connection.isConnected();
		return isConnected;
	}
	
	/**
	 * Close this connection
	 */
	public void close() {
		isConnected = false;
		try {
			connection.close();
		} catch (Exception e) {}
		try {
			reader.close();
		} catch (Exception e) {}
		try {
			writer.close();
		} catch (Exception e) {}
	}
	
	/**
	 * Sends the given message to the host this ClientConnection is connected to
	 * @param msg	The message to send
	 */
	public void send(String msg) {
		writer.println(msg);
	}
	
	/**
	 * Tries to receive a message from the host that this ClientConnection is connected to.
	 * Will block while receiving
	 * @throws IOException if an error occurs while trying to receive message.
	 * @return	The received message
	 */
	public String receive() throws IOException {
		String temp;
		Debugger.print("Waiting (0)..");
		while((temp = reader.readLine()) == null) {
			Debugger.print("Waiting (1)..");
			// TODO: Add timeout!
		}
		Debugger.print("Receive: " + temp);
		return temp;
	}
}
