package session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import network.Debugger;

import logic.Debt;
import logic.DebtStatus;
import logic.User;

import requests.FriendRequest;
import requests.LogInRequest;
import requests.LogInRequestStatus;
import requests.xml.XMLSerializable;

public abstract class Session {

	private User user;
	public static Session session;

	/**
	 * Connects to the given host at the given port, if not already connected to a host
	 * @param host
	 * @param port
	 */
	public abstract void connect(String host, int port);
	
	/**
	 * Checks if this Session's user's connection is connected to the server
	 * @return	True if connected, false if not
	 */
	public abstract boolean isConnected();
	
	/**
	 * Sets up the session by initializing the static session variable, and some member variables.
	 */
	public abstract void init();
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
	
	/**
	 * Checks if this Session's user is logged in
	 * @return	True if the user is logged in, false if not
	 */
	public boolean isLoggedIn() {
		return user != null;
	}
	
	/**
	 * Sends the given message to the connected host
	 * @param msg	The message to send
	 */
	public abstract void send(String msg);
	
	/**
	 * Tries to receive a message from the connected host
	 * @throws IOException if an error occurs while trying to receive messages
	 * @return The received message
	 */
	public abstract String receive() throws IOException;
	
	/**
	 * Sends the given message.
	 * Will receive and return a response if specified
	 * 
	 * @param msg			The message to send
	 * @param shouldReceive	If we should receive and return a response
	 * @return				The received message, or null of not 
	 * @throws IOException	If the connection could not be established
	 */
	public abstract String sendAndReceive(String msg) throws IOException;
	
	/**
	 * Tries to log in by sending a LogInRequest to the server connected to by the connection
	 * @param username		The user name
	 * @param password		The password
	 * @return				The status of the received response
	 */
	public LogInRequestStatus logIn(String username, String password) {
		LogInRequest resp = null;
		try {
			resp = (LogInRequest) XMLSerializable.toObject(sendAndReceive(new LogInRequest(username, password).toXML()));
			if(resp.isAccepted()) {
				setUser(resp.getUser());
			}
			return resp.getStatus();
		} catch(IOException e) {
			// TODO 
			e.printStackTrace();
			Debugger.print(e.toString());
		}
		return LogInRequestStatus.UNHANDLED;
	}
	
	public void processUpdate(Object o) {
		System.out.println("Update received: " + o);
		if(o instanceof Debt) {
			Debt d = (Debt) o;
			if(d.getStatus() == DebtStatus.MERGE) {
				System.out.println("Received a merged debt!");
				// Remove all debts confirmed between these two users with the same currency, that are not completed by any users
				List<Debt> debtsToRemove = new ArrayList<Debt>();
				for (int i = 0; i < getUser().getNumberOfConfirmedDebts(); i++) {
					Debt c = getUser().getConfirmedDebt(i); 
					if(c.getStatus() == DebtStatus.CONFIRMED) {
						if((c.getFrom().equals(d.getFrom()) && c.getTo().equals(d.getTo())) || (c.getFrom().equals(d.getTo()) && c.getTo().equals(d.getFrom()))) {
							debtsToRemove.add(c);
						}
					}
				}
				for (Debt i : debtsToRemove) {
					getUser().removeConfirmedDebt(i);
				}
				// Also remove the pending debt that was accepted
				getUser().removePendingDebt(d);
				// Add the new merged debt if it's amount is not zero
				if(Math.abs(d.getAmount()) != 0) {
					d.setStatus(DebtStatus.CONFIRMED);
				} else {
					d.setStatus(DebtStatus.COMPLETED);
				}
				getUser().addConfirmedDebt(d);
				return;
			}
			for (int i = 0; i < (user.getNumberOfConfirmedDebts() > user.getNumberOfPendingDebts() ? user.getNumberOfConfirmedDebts(): user.getNumberOfPendingDebts()); i++) {
				if(user.getNumberOfConfirmedDebts() > i && user.getConfirmedDebt(i).getId() == d.getId()) {
					user.removeConfirmedDebt(i);
					user.addConfirmedDebt(d);
					return;
				}
				if(user.getNumberOfPendingDebts() > i && user.getPendingDebt(i).getId() == d.getId()) {
					user.removePendingDebt(i);
					if(d.isConfirmed()) user.addConfirmedDebt(d);
					else if(d.getStatus() != DebtStatus.DECLINED) user.addPendingDebt(d);
					return;
				}
			}
			// Should probably add the debt in one of the lists if the method reaches this far?
			if(d.isConfirmed()) user.addConfirmedDebt(d);
			else user.addPendingDebt(d);
		} else if(o instanceof FriendRequest) {
			FriendRequest req = (FriendRequest) o;
			switch(req.getStatus()) {
			case DECLINED:
				// TODO: Notify user (or?)
				break;
			case ACCEPTED:
				// Someone accepted our friend request, add him/her as friend
				getUser().addFriend(new User(req.getFriendUsername()));
				break;
			case PENDING:
				// We received a new friend request, add it
				getUser().addFriendRequest(req);
				break;
			}
		} else {
			System.out.println("ERROR: Received something unknown!");
		}
	}
	
}
