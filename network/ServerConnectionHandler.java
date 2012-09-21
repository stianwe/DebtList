package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import logic.User;

import requests.CreateUserRequest;
import requests.LogInRequest;
import requests.LogInRequestStatus;
import requests.XMLParsable;

public class ServerConnectionHandler extends Thread {

	private Socket connection;
	private ServerConnection serverConnection;
	private BufferedReader reader;
	private PrintWriter writer;
	private User user;
	private boolean running;
	
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
		running = true;
		System.out.println("ServerConnectionHandler running!");
		String xml;
		while(running && (xml = receive()) != null) {
			System.out.println("Received XML: " + xml);
			// Receive LogInRequest
			try {
				Object o = XMLParsable.toObject(xml);
				if(o instanceof LogInRequest) {
					System.out.println("Received log in request!");
					LogInRequest req = (LogInRequest)o;
					User user = serverConnection.getUser(req.getUserName());
					if(user == null) {
						System.out.println("User not found!");
					}
					if(user != null && user.getUsername().equals(req.getUserName()) && user.getPassword().equals(req.getPassword()) && !user.isOnline()) {
						// TODO: Add all the users variables before sending the response back!
						System.out.println("Log in OK!");
						user.setIsOnline(true);
						this.user = user;
						req.setAccepted(true);
						req.setStatus(LogInRequestStatus.ACCEPTED);
						if(req.isAccepted()) {
							System.out.println("Log in is set to accepted!");
						}
					} else if(user != null && user.isOnline()){
						req.setStatus(LogInRequestStatus.ALREADY_LOGGED_ON);
						System.out.println("User already online.");
					} else {
						req.setStatus(LogInRequestStatus.WRONG_INFORMATION);
						System.out.println("Username or password failed");
					}
					String temp = req.toXml();
					System.out.println("Sending XML: " + temp);
					send(temp);
				} else if(o instanceof CreateUserRequest) {
					CreateUserRequest cur = (CreateUserRequest) o;
					if(serverConnection.getUser(cur.getUsername()) == null) {
						// TODO: Add check on username
						serverConnection.addUser(cur.getRequestedUser());
						cur.setIsAproved(true);
					}
					String temp = cur.toXml();
					System.out.println("Sending XML: " + temp);
					send(temp);
				} else {
					System.out.println("Received something unknown!");
					// TODO
				}
			} catch(Exception e) {
				// TODO
				System.out.println("Exception: " + e);
			}
		}
		// TODO
		System.out.println("Killing thread.");
		running = false;
	}
	
	public String receive() {
		try {
			return reader.readLine();
		} catch (IOException e) {
			System.out.println("User disconnected!");
			if(this.user != null) {
				this.user.setIsOnline(false);
			}
			running = false;
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
