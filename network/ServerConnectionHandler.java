package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import logic.Debt;
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
	private UpdateSender updateSender;
	
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
	
	public void sendUpdate(String xml) {
		updateSender.send(xml);
	}
	
	public User getUser() {
		return user;
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
				// TODO: remember to notify affected users if they are online
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
						updateSender = new UpdateSender(connection.getInetAddress().toString(), req.getUpdatePort());
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
				} else if(o instanceof Debt) {
					Debt d = (Debt) o;
					if(d.getId() == -1) {
						// This is a request to create a new debt
						// Validate that this is a valid debt
						boolean valid = true;
						if(d.getRequestedBy().getUsername().equals(user.getUsername())) {
							if(d.getTo().getUsername().equals(user.getUsername()) && user.getFriend(d.getFrom().getUsername()) == null) {
								valid = false;
							} else if(d.getFrom().getUsername().equals(user.getUsername()) && user.getFriend(d.getTo().getUsername()) == null) {
								valid = false;
							} else {
								valid = false;
							}
							if(d.isConfirmed()) {
								valid = false;
							}
						} else valid = false;
						if(valid) {
							d.setId(serverConnection.getNextDebtId());
							// Notify other user
							serverConnection.notifyUser((d.getTo().getUsername().equals(user.getUsername()) ? d.getFrom().getUsername() : d.getTo().getUsername()), d);
						}
						send(d.toXml());
					} 
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
