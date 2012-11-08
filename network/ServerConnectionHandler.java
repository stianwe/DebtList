package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import console.Main;

import logic.Debt;
import logic.DebtStatus;
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
	private Thread updateSenderThread;
	
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
	
	public void setUser(User u) {
		this.user = u;
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
				System.out.println("Done parsing object!");
				// TODO: remember to notify affected users if they are online
				if(o instanceof LogInRequest) {
					processLoginRequest((LogInRequest) o);
				} else if(o instanceof CreateUserRequest) {
					processCreateUserRequest((CreateUserRequest) o);
				} else if(o instanceof Debt) {
					processDebt((Debt) o);
				} else {
					System.out.println("Received something unknown!");
					// TODO
				}
			} catch(Exception e) {
				// TODO
				System.out.println("Exception: " + e);
				e.printStackTrace();
			}
		}
		// TODO
		System.out.println("Killing UpdateSender.");
		updateSender.setRunning(false);
		updateSenderThread.interrupt();
		System.out.println("Killing thread.");
		running = false;
	}
	
	public void processCreateUserRequest(CreateUserRequest req) {
		if(serverConnection.getUser(req.getUsername()) == null) {
			// TODO: Add check on username
			serverConnection.addUser(req.getRequestedUser());
			req.setIsAproved(true);
		}
		String temp = req.toXml();
		System.out.println("Sending XML: " + temp);
		send(temp);
	}
	
	public void processLoginRequest(LogInRequest req) {
		System.out.println("Received log in request!");
		User user = serverConnection.getUser(req.getUserName());
		if(user == null) {
			System.out.println("User not found!");
		}
		if(user != null && user.getUsername().equals(req.getUserName()) && user.getPassword().equals(req.getPassword()) && !user.isOnline()) {
			// TODO: Add all the user's variables before sending the response back!
			System.out.println("Log in OK!");
			user.setIsOnline(true);
			this.user = user;
			req.setAccepted(true);
			req.setStatus(LogInRequestStatus.ACCEPTED);
			if(req.isAccepted()) {
				System.out.println("Log in is set to accepted!");
			}
			// Load the user variables
			req.setUser((User) user.toSendable(true));
			// Now that the user is logged in, we can start a updateSender
			updateSender = new UpdateSender(connection.getInetAddress().getHostAddress(), req.getUpdatePort());
			updateSenderThread = new Thread(updateSender);
			updateSenderThread.start();
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
	}
	
	public void processDebt(Debt d) {
		switch(d.getStatus()) {
		case REQUESTED:
			processRequestedDebt(d);
			break;
		case CONFIRMED:
		case DECLINED:
			processConfirmedDeclinedDebt(d);
			break;
		case COMPLETED_BY_FROM:
		case COMPLETED_BY_TO:
			processCompletedDebt(d);
			break;
		case COMPLETED:
			// TODO: Do we need to process this? No!
			break;
		}
	}

	public void processCompletedDebt(Debt d) {
		// TODO: Verify!!!!
		Debt old = null;
		for (int i = 0; i < getUser().getNumberOfConfirmedDebts(); i++) {
			if(getUser().getConfirmedDebt(i).getId() == d.getId()) old = getUser().getConfirmedDebt(i);
		}
		if(old == null) {
			System.out.println("Something wrong happened while processing completedDebt");
			Main.printDebts(getUser().getConfirmedDebts(), "Confirmed debts");
			Main.printDebts(getUser().getPendingDebts(), "Pending debts");
			return;
		}
		// Check that this user has not already completed this debt. NO why should we? (Not this way at least.)
//		if((d.getTo().equals(getUser()) && d.getStatus() == DebtStatus.COMPLETED_BY_TO) || (d.getFrom().equals(getUser()) && d.getStatus() == DebtStatus.COMPLETED_BY_FROM)) {
//			// TODO: Then what? Send back a correct version of the debt?
//			System.out.println("Completing of debt failed, because this user has already marked the debt as complete");
//			return;
//		}
		if((old.getStatus() == DebtStatus.COMPLETED_BY_FROM && d.getStatus() == DebtStatus.COMPLETED_BY_TO) || (old.getStatus() == DebtStatus.COMPLETED_BY_TO && d.getStatus() == DebtStatus.COMPLETED_BY_FROM)) {
			d.setStatus(DebtStatus.COMPLETED);
		} 
		old.setStatus(d.getStatus());
		serverConnection.notifyUser((old.getTo().equals(getUser()) ? old.getFrom() : old.getTo()).getUsername(), old);
		send(old.toSendable(false).toXml());
	}
	
	public void processConfirmedDeclinedDebt(Debt d) {
		// TODO: Verify!!
		// Find our instance of the debt
		// We assume that it is pending, or else why would someone accept or decline it?
		Debt our = null;
		for (int i = 0; i < getUser().getNumberOfPendingDebts(); i++) {
			if(getUser().getPendingDebt(i).getId() == d.getId()) our = getUser().getPendingDebt(i); 
			System.out.println("Checked: " + getUser().getPendingDebt(i).getId() + " should find: " + d.getId());
		}
		if(our == null) {
			// Something wrong has happened! This debt was not ours, or not pending.
			// TODO Do nothing?
			System.out.println("SOMETHING WRONG HAPPENED WHILE PROCESSING CONFIRMED OR DECLINED DEBT!");
			return;
		}
		our.setStatus(d.getStatus());
		// Let the requesting user know about the accept/decline
		serverConnection.notifyUser(d.getRequestedBy().getUsername(), our.toSendable(true));
		// Remove the debt from the pending list (since it is now confirmed or declined)
		User other = serverConnection.getUser((our.getFrom().equals(getUser()) ? our.getTo() : our.getFrom()).getUsername());
		System.out.println("Other user is: " + other.getUsername());
		getUser().removePendingDebt(our);
		if(other.removePendingDebt(our)) System.out.println("Other's debt was removed!");
		else System.out.println("Other's debt was NOT(!!!!!!!!!!!) removed!");
		if(our.getStatus() == DebtStatus.CONFIRMED) {
			// If the debt is now confirmed, we must move it to the correct lists
			getUser().addConfirmedDebt(our);
			// For both users
			other.addConfirmedDebt(our);
		} else {
			// If the debt was deleted we simply let it be removed..
		}
		send(d.toSendable(false).toXml());
		// TODO Anything else?
	}
	
	public void processRequestedDebt(Debt d) {
		// Validate that this is a valid debt
		boolean valid = true;
		System.out.println("Checkin if new debt is valid..");
		// Check that this user requested the debt
		if(d.getRequestedBy().getUsername().equals(user.getUsername())) {
			// Check if this user is the receiver of the debt, and if the sender is a friend
			if(d.getTo().getUsername().equals(user.getUsername()) && user.getFriend(d.getFrom().getUsername()) == null) {
				System.out.println("1");
				valid = false;
			// Check if this user is the sender of the debt, and the receiver is a friend
			} else if(d.getFrom().getUsername().equals(user.getUsername()) && user.getFriend(d.getTo().getUsername()) == null) {
				System.out.println("2");
				valid = false;
			} else {
//				System.out.println("3");
//				valid = false;
			}
			if(d.isConfirmed()) {
				System.out.println("4");
				valid = false;
			}
		} else valid = false;
		System.out.println("New debt is valid? " + valid);
		if(valid) {
			d.setId(serverConnection.getNextDebtId());
			System.out.println("id set to: " + d.getId());
			// Save the debt
			serverConnection.getUser((getUser().equals(d.getTo()) ? d.getFrom().getUsername() : d.getTo().getUsername())).addPendingDebt(d);
			System.out.println("Added debt to " + getUser().getUsername() + " and " + serverConnection.getUser((getUser().equals(d.getTo()) ? d.getFrom().getUsername() : d.getTo().getUsername())).getUsername());
			getUser().addPendingDebt(d);
//			System.out.println(serverConnection.getUser((getUser().equals(d.getTo()) ? d.getFrom().getUsername() : d.getTo().getUsername())) == serverConnection.getHandler(serverConnection.getUser((getUser().equals(d.getTo()) ? d.getFrom().getUsername() : d.getTo().getUsername())).getUsername()).getUser());
			// Notify other user
			serverConnection.notifyUser((d.getTo().getUsername().equals(user.getUsername()) ? d.getFrom().getUsername() : d.getTo().getUsername()), d);
		}
		send(d.toXml());
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
