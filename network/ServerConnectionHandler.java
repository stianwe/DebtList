package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import logic.User;

import requests.LogInRequest;
import requests.XMLParsable;

public class ServerConnectionHandler extends Thread {

	private Socket connection;
	private ServerConnection serverConnection;
	private BufferedReader reader;
	private PrintWriter writer;
	
	public ServerConnectionHandler(Socket connection, ServerConnection serverConnection) {
		this.connection = connection;
		this.serverConnection = serverConnection;
		serverConnection.addConnectionHandler(this);
		try {
		reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			writer = new PrintWriter(connection.getOutputStream(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		String xml;
		while((xml = receive()) != null) {
			// Receive LogInRequest
			try {
				Object o = XMLParsable.toObject(xml);
				if(o instanceof LogInRequest) {
					LogInRequest req = (LogInRequest)o;
					User user = serverConnection.getUser(req.getUserName());
					if(user != null && user.getPassword().equals(req.getPassword())) {
						req.setAccepted(true);
						send(req.toXml());
					}
				} else {
					// TODO
				}
			} catch(Exception e) {
				// TODO
			}
		}
		// TODO
	}
	
	public String receive() {
		try {
			return reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public void send(String msg) {
		writer.println(msg);
	}
	
	public void close() {
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
}
