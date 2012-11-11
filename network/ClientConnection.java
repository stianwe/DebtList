package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
	public void connect(String host, int port) {
		isConnected = false;
		try {
			connection = new Socket(host, port);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			writer = new PrintWriter(connection.getOutputStream(), true);
			isConnected = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				connection.close();
			} catch (Exception ex) {}
		}
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
		System.out.println("Waiting for answer (1)..");
		while((temp = reader.readLine()) == null) {
			// TODO: Add timeout!
			System.out.println("Waiting for answer (2)..");
		}
		System.out.println("Receive: " + temp);
		return temp;
	}
}
