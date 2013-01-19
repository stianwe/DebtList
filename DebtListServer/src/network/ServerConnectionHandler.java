package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import logic.Debt;
import logic.DebtStatus;
import logic.User;
import requests.CreateUserRequest;
import requests.FriendRequest;
import requests.LogInRequest;
import requests.LogInRequestStatus;
import requests.UpdateRequest;
import requests.FriendRequest.FriendRequestStatus;
import requests.xml.XMLSerializable;

public class ServerConnectionHandler extends Thread {

	private Socket connection;
	private ServerConnection serverConnection;
	private BufferedReader reader;
	private PrintWriter writer;
	private User user;
	private boolean running;
	private UpdateRequest update;
	private long timeOfLastCommand = 0;
	
	public ServerConnectionHandler(Socket connection, ServerConnection serverConnection) {
		this.connection = connection;
		update = new UpdateRequest();
		this.serverConnection = serverConnection;
		serverConnection.addConnectionHandler(this);
		try {
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			writer = new PrintWriter(connection.getOutputStream(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			serverConnection.writeToLog("Failed while initializing handler: " + e.toString());
		}
	}
	
	/**
	 * Updates the time of last command to the current time
	 */
	public void updateTimeOfLastCommand() {
		timeOfLastCommand = System.currentTimeMillis();
	}
	
	/**
	 * @return Time of the last command (in millisec)
	 */
	public long getTimeOfLastCommand() {
		return timeOfLastCommand;
	}
	
	/**
	 * Adds the given object to this user's send queue
	 * @param o	The object to send
	 */
	public void sendUpdate(XMLSerializable o) {
		update.add(o);
	}
	
	/**
	 * @return	This ServerConnectionHandler's user
	 */
	public User getUser() {
		return user;
	}
	
	/**
	 * This method will try to receive messages from the connected client, and will pass the messages to the appropriate process method.
	 * Will run until stopped.
	 */
	@Override
	public void run() {
		running = true;
		System.out.println("ServerConnectionHandler running!");
		String xml;
		while(running && (xml = receive()) != null) {
			// Register the time of the command
			// FIXME: This should be uncommented!
			updateTimeOfLastCommand();
			System.out.println("Received XML: " + xml);
			try {
				XMLSerializable o = XMLSerializable.toObject(xml);
				System.out.println("Done parsing object!");
				// Process token if any is attached
				if(!processToken(o.getSessionToken())) { 
					die(false);
					System.out.println("Should not happen..");
					return;
				}
				// Receive LogInRequest
				if(o instanceof LogInRequest) {
					System.out.println("Received login request!");
					processLoginRequest((LogInRequest) o);
				} else if(o instanceof CreateUserRequest) {
					System.out.println("Received create user request!");
					processCreateUserRequest((CreateUserRequest) o);
				} else {
					// Check that the connected user is logged in before processing any of these requests
					// TODO: Send error message to user?
					if(this.user == null || !this.user.isOnline()) {
						System.out.println("User not logged in. Not processing message!");
						continue;
					}
					if(o instanceof Debt) {
						System.out.println("Received debt!");
						processDebt((Debt) o);
					} else if(o instanceof FriendRequest) {
						System.out.println("Received friend request!");
						processFriendRequest((FriendRequest) o);
					} else if(o instanceof UpdateRequest) {
						System.out.println("Received update request!");
						processUpdate();
					} else {
						System.out.println("Received something unknown!");
						serverConnection.writeToLog("Received something unknown: " + xml);
						// TODO
					}
				}
			} catch(Exception e) {
				// TODO
				System.out.println("Exception: " + e);
				e.printStackTrace();
				serverConnection.writeToLog("Failed to parse/process XML: " + e.toString());
			}
		}
		die(true);
	}
	
	/**
	 * 
	 * @param token
	 * @return		Should continue
	 */
	private boolean processToken(String token) {
		if(token != null) {
			if(token.equals(Constants.SESSION_TOKEN_REQUEST))
				return true;
			System.out.println("Token detected! " + token);
			// Check if this is our connection
			ServerConnectionHandler handler = serverConnection.getTokenManager().getHandler(token);
			if(handler == this) {
				// Keep going and process the object
				System.out.println("Token matched to handler! This should probably never happen..");
			} else {
				// If not, check if it has a handler
				if(handler != null) {
					// We hijack this user
					// FIXME!!!! UNTESTED!
					this.update = handler.getUpdate();
					this.user = handler.getUser();
					this.user.setIsOnline(true);
					System.out.println("Hijacking user: " + (this.user != null ? this.user.getUsername() : "null")+ "!");
					serverConnection.getTokenManager().remove(token);
					serverConnection.getTokenManager().registerToken(token, this);
					System.out.println("Killing old handler.");
					handler.die(false);
					return true;
				} else {
					// If it has no handler, we take it
					// Make sure it gets all necessary updates by just throwing in all relevant objects
					// FIXME!!!!! UNTESTED!
					// FIXME!! Should we really just take it, if it has no handler?
					System.out.println("User needs a new handler.");
					// Add all debts
					// Set the user
					String username = serverConnection.getTokenManager().getUsername(token);
					if(username == null) {
						serverConnection.writeToLog("Something wrong happened while taking over a session! Username was null!");
						return false;
					}
					user = serverConnection.getUser(username);
					// And process the received object as normal
					for (int i = 0; i < getUser().getNumberOfConfirmedDebts(); i++) {
						update.add(getUser().getConfirmedDebt(i));
					}
					for (int i = 0; i < getUser().getNumberOfPendingDebts(); i++) {
						update.add(getUser().getPendingDebt(i));
					}
					// Add all friend requests
					for (int i = 0; i < getUser().getNumberOfFriendRequests(); i++) {
						update.add(getUser().getFriendRequest(i));
					}
					return true;
				}
			}
		} // If not, we just handle it as normal
		return true;
	}
	
	public UpdateRequest getUpdate() {
		return update;
	}
	
	private void die(boolean logOffUser) {
		System.out.println("Killing thread.");
		// Check if user was online
		if(logOffUser) { 
			if(this.getUser() != null) {
				// Then set it offline
				this.getUser().setIsOnline(false);
			}
		}
		running = false;
		// Remove ourself from the handler list
		serverConnection.removeConnectionHandler(this);
	}
	
	public void processUpdate() {
		// Send the update
		send(update.toXML());
		// Clear the update
		update.clear();
	}
	
	/**
	 * Process the given FriendRequest.
	 * Will reply with a request with the following status:
	 * 	- This is a requests to create a friend request:
	 * 		o USER_NOT_FOUND if the requesting friend was not found in the server's database
	 * 		o PENDING if the request has been sent to the target user (friend)
	 * 		o UNHANDLED if the request was not valid
	 * 	- This is a response to a friend request:
	 * 		o A copy of the request received
	 * @param request	The FriendRequest
	 */
	public void processFriendRequest(FriendRequest request) {
		// TODO: Don't let a user send friend requests to the same user twice.
		// Validate
		boolean valid = true;
		// TODO: This should be unnecessary (should be able to use this.getUser())
		User thisUser = serverConnection.getUser(this.getUser().getUsername());
		// Check that this is the requesting user's handler if this is a new request
		if(request.getStatus() == FriendRequestStatus.UNHANDLED && !request.getFromUser().equals(this.getUser())) valid = false;
		// Check that this is the target user's handler if this is a response
		else if((request.getStatus() == FriendRequestStatus.ACCEPTED || request.getStatus() == FriendRequestStatus.DECLINED) && !request.getFriendUsername().equals(this.getUser().getUsername()))
			valid = false;
		else if(request.getStatus() == FriendRequestStatus.PENDING || request.getStatus() == FriendRequestStatus.USER_NOT_FOUND)
			valid = false;
		// Check that the FriendRequest's target exists
		if(serverConnection.getUser(request.getFriendUsername()) == null) {
			valid = false;
			request.setStatus(FriendRequestStatus.USER_NOT_FOUND);
		}
		//CHECKING IF THE FRIEND REQUEST IS AN ALREADY ACCEPTED FRIEND
		for(int i=0; i<thisUser.getNumberOfFriends(); i++){
			if(request.getFriendUsername().equals(thisUser.getFriend(i).getUsername())){
				valid=false;
			}
		}
		// Check that the sending user is allowed to set the given status
		if(request.getStatus() == FriendRequestStatus.UNHANDLED && !request.getFromUser().equals(this.getUser())) valid = false;
		if((request.getStatus() == FriendRequestStatus.ACCEPTED || request.getStatus() == FriendRequestStatus.DECLINED) && !request.getFriendUsername().equals(this.getUser().getUsername())) {
			valid = false;
		}
		if(request.getStatus() == FriendRequestStatus.USER_NOT_FOUND) valid = false;
		// If this is a response to a friend request, check that this has a corresponding friend request
		if((request.getStatus() == FriendRequestStatus.ACCEPTED || request.getStatus() == FriendRequestStatus.DECLINED) && !thisUser.hasFriendRequest(request)) {
			valid = false;
		}
		User otherUser = serverConnection.getUser((request.getFromUser().equals(this.getUser()) ? request.getFriendUsername() : request.getFromUser().getUsername()));
		// If this is a new friend request, check that these two users don't already have any requests for each other, or that the user is sending a request to himself
		if(request.getStatus() == FriendRequestStatus.UNHANDLED) {
			if(thisUser.hasFriendRequest(request) || otherUser.hasFriendRequest(request) ||
					// Check the oposite way too
					thisUser.hasOppositeFriendRequest(request) || otherUser.hasOppositeFriendRequest(request)) {
				valid = false;
				request.setStatus(FriendRequestStatus.ALREADY_EXISTS);
			}
			if(request.getFriendUsername().equals(thisUser.getUsername()))
				valid = false;
		}
		if(valid) {
			System.out.println("FriendRequest was valid.");
			// If this is a new friend request..
			if(request.getStatus() == FriendRequestStatus.UNHANDLED) {
				System.out.println("This was a new friend request.");
				// Give it an id
				request.setId(serverConnection.getNextFriendRequestId());
				// Set the correct status
				request.setStatus(FriendRequestStatus.PENDING);
				// Add it to the target friend
				serverConnection.getUser(request.getFriendUsername()).addFriendRequest(request);
			} else {
				System.out.println("This was a reply to a friend request.");
				// If this is a accepted/declined friend request, update the requesting user's friends (if accepted, if not accepted we do nothig except to remove the request)
				if(request.getStatus() == FriendRequestStatus.ACCEPTED) {
					// Add friends
					otherUser.addFriend(thisUser);
					thisUser.addFriend(otherUser);
				} 
				// Remove friend request.. NO!
//				thisUser.removeFriendRequest(request);
				// Copy status
				thisUser.getFriendRequestFrom(otherUser.getUsername()).setStatus(request.getStatus());
			}
			// Notify other user
			serverConnection.notifyUser(otherUser.getUsername(), request);
		} else {
			System.out.println("FriendRequest was not valid.");
			serverConnection.writeToLog("Received invalid friend request from: " + thisUser.getUsername() + ": " + request.toXML());
			// Send some garbage that will trigger an error
			// TODO Set a crap status?? But don't overwrite already set error status!
		}
		send(request.toXML());
	}
	
	/**
	 * Process the given CreateUserRequest
	 * @param req	The CreateUserRequest
	 */
	public void processCreateUserRequest(CreateUserRequest req) {
		// Check that the user don't already exist
		if(serverConnection.getUser(req.getUsername()) == null &&
			// And that the user name does not exceed 30 characters
				req.getUsername().length() <= 30) {
			// TODO: Add check on username
			// Get an id for the user
			req.getRequestedUser().setId(serverConnection.getNextUserId());
			// Notify the server of the new user
			serverConnection.addUser(req.getRequestedUser(), req.getPassword());
			// Set the request as approved
			req.setIsAproved(true);
		}
		// Reply with an answer
		send(req.toXML());
	}
	
	/**
	 * @return	The IP address of the connected user
	 */
	public String getUserIp() {
		return connection.getInetAddress().getHostAddress();
	}
	
	/**
	 * Process the given LogInRequest by setting this ServerConnectionHandler's user if login is correct.
	 * Will also on correct login start a UpdateSender for this connection at the port specified in the LogInRequest
	 * @param req	The LogInRequest
	 */
	public void processLoginRequest(LogInRequest req) {
		System.out.println("Received log in request!");
		User user = serverConnection.getUser(req.getUserName());
		if(user == null) {
			System.out.println("User not found!");
		}
		if(user != null && user.getUsername().equals(req.getUserName()) 
				&& serverConnection.checkPassword(user, req.getPassword()) 
				&& !user.isOnline()) {
			System.out.println("Log in OK!");
			user.setIsOnline(true);
			this.user = user;
			req.setAccepted(true);
			req.setStatus(LogInRequestStatus.ACCEPTED);
			// Load the user variables
			req.setUser((User) user);
			if(req.isAccepted()) {
				System.out.println("Log in is set to accepted!");
				// Assign session token if requested
				if(req.getSessionToken() != null && req.getSessionToken().equals(Constants.SESSION_TOKEN_REQUEST)) {
					System.out.println("Token request received.");
					String token = serverConnection.getTokenManager().generateToken(this);
					System.out.println("Token granted to " + user.getUsername() + ": " + token);
					req.setSessionToken(token);
				}
			}
		} else if(user != null && user.isOnline()){
			req.setStatus(LogInRequestStatus.ALREADY_LOGGED_ON);
			System.out.println("User already online.");
		} else {
			req.setStatus(LogInRequestStatus.WRONG_INFORMATION);
			System.out.println("Username or password failed");
		}
		String temp = req.toXML();
		System.out.println("Sending XML: " + temp);
		send(temp);
	}
	
	/**
	 * Process the given debt
	 * @param d	The debt
	 */
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
//			Main.printDebts(getUser().getConfirmedDebts(), "Confirmed debts");
//			Main.printDebts(getUser().getPendingDebts(), "Pending debts");
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
		send(old.toXML());
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
			serverConnection.writeToLog("Something wrong happened while processing confirmed or declined debt for " + getUser().getUsername());
			return;
		}
		our.setStatus(d.getStatus());
		// Remove the debt from the pending list (since it is now confirmed or declined)
		User other = serverConnection.getUser((our.getFrom().equals(getUser()) ? our.getTo() : our.getFrom()).getUsername());
		System.out.println("Other user is: " + other.getUsername());
		getUser().removePendingDebt(our);
		if(other.removePendingDebt(our)) System.out.println("Other's debt was removed!");
		else System.out.println("Other's debt was NOT(!!!!!!!!!!!) removed!");
		if(our.getStatus() == DebtStatus.CONFIRMED) {
			d = mergeDebts(our);
			
			// No longer needed, since mergeDebts does this for us
//			// If the debt is now confirmed, we must move it to the correct lists
//			getUser().addConfirmedDebt(our);
//			// For both users
//			other.addConfirmedDebt(our);
		} else {
			// If the debt was deleted we simply let it be removed..
		}
		// Let the requesting user know about the accept/decline
		serverConnection.notifyUser(d.getRequestedBy().getUsername(), d);
		send(d.toXML());
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
		} else {
			serverConnection.writeToLog("Received invalid debt from " + getUser().getUsername());
		}
		send(d.toXML());
	}
	
	/**
	 * 
	 * @param d
	 * @return	The debt to send to the clients (no matter if any merging was done)
	 */
	public Debt mergeDebts(Debt d) {
		User thisUser = serverConnection.getUser(this.getUser().getUsername());
		User otherUser = serverConnection.getUser((d.getTo().equals(thisUser) ? d.getFrom() : d.getTo()).getUsername());
		List<Debt> debtsToMerge = new ArrayList<Debt>();
		// Check if these two users already have debts between them
		// Pending debts
		for (int i = 0; i < thisUser.getNumberOfConfirmedDebts(); i++) {
			Debt tDebt = thisUser.getConfirmedDebt(i);
			if(tDebt.getTo().equals(otherUser) || tDebt.getFrom().equals(otherUser)) {
				// Check if this debt uses the same currency
				if(tDebt.getWhat().equalsIgnoreCase(d.getWhat())) {
					// And check that it is not completed by any of the usersarg0
					if(tDebt.getStatus() == DebtStatus.CONFIRMED) {
						debtsToMerge.add(tDebt);
					}
				}
			}
		}
		if(!debtsToMerge.isEmpty()) {
			d.setComment('"' + d.getComment() + '"');
		}
		for (Debt debtToMerge : debtsToMerge) {
			// Remove debts from users .. NO! Set them as deleted
			thisUser.getConfirmedDebtById(debtToMerge.getId()).setStatus(DebtStatus.DELETED);
			otherUser.getConfirmedDebtById(debtToMerge.getId()).setStatus(DebtStatus.DELETED);
//			thisUser.removeConfirmedDebt(debtToMerge);
//			otherUser.removeConfirmedDebt(debtToMerge);
			// Merge amount
			if(d.getTo().equals(debtToMerge.getTo())) {
				d.setAmount(d.getAmount() + debtToMerge.getAmount());
			} else {
				d.setAmount(d.getAmount() - debtToMerge.getAmount());
			}
			// Merge comments
			d.setComment(d.getComment() + " " + '"' + debtToMerge.getComment() + '"');
		}
		// Check if amount is negative
		if(d.getAmount() < 0) {
			// Swap to and from users
			User temp = d.getFrom();
			d.setFrom(d.getTo());
			d.setTo(temp);
			d.setAmount(d.getAmount() * -1);
		}
		// Check that amount is greater than zero
		if(Math.abs(d.getAmount()) > 0) {
			// Add the debt to the users
			thisUser.addConfirmedDebt(d);
			otherUser.addConfirmedDebt(d);
		}
		if(!debtsToMerge.isEmpty()) {
			System.out.println("This was a merge!");
			return new Debt(d.getId(), d.getAmount(), d.getWhat(), d.getFrom(), d.getTo(), d.getComment(), d.getRequestedBy(), DebtStatus.MERGE);
		} else {
			System.out.println("This was NOT a merge!");
			return d;
		}
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
		System.out.println("Sedning: " + msg);
		writer.println(msg);
	}
	
	/**
	 * Attempts to close the connection, log off the user and stop the thread.
	 */
	public void close() {
		running = false;
		try {
			connection.close();
		} catch (Exception e) {}
		try {
			reader.close();
		} catch (Exception e) {}
		try {
			writer.close();
		} catch (Exception e) {}
		if(this.user != null) {
			this.user.setIsOnline(false);
		}
	}
}
