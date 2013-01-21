package console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import network.Constants;
import network.Debugger;

import logic.Debt;
import logic.DebtStatus;
import logic.User;
import requests.CreateUserRequest;
import requests.FriendRequest;
import requests.LogInRequestStatus;
import requests.FriendRequest.FriendRequestStatus;
import requests.xml.XMLSerializable;
import session.PCSession;
import session.Session;
import session.Updater;

public class Main {

	public static Updater updater = new Updater();
	
	public static void main(String[] args) {
		new PCSession().init();
		
		System.out.println("Welcome to DebtList (version 0)!");
		System.out.println("Connect to server by typing " + '"' + "connect" + '"' + " followed by " + '"' + "login" + '"' + " to log in, or " + '"' + "create user" + '"' + " to create a new user.");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String command = null;
		do {
			System.out.print("> ");
			try {
				command = reader.readLine();
			} catch (IOException e) {
				System.out.println("Syntax error: " + e);
				continue;
			}
		} while(!processCommand(command));
		// Kill the update poller
		Debugger.print("Killing updater..");
		if(updater.stopUpdater()) Debugger.print("Successfully stopped the updater.");
		else Debugger.print("Failed to stop the updater!");
		System.out.println("Bye!");
	}
	
	/**
	 * Processes the given command.
	 * @param command	The command to process
	 * @return			True if the command was "exit", false if not
	 */
	public static boolean processCommand(String command) {
		// Commands that are accessible both if the user is logged in or not
		if(command.equals("exit")) return true;
		else if(command.equals("debug start")) Debugger.setDebug(true);
		else if(command.equals("debug stop")) Debugger.setDebug(false);
		else {
			// Commands that only are accessible when the user is NOT logged in
			if(!PCSession.session.isLoggedIn()) {
				if(command.equals("create user") && PCSession.session.isConnected()) safeCreateUser();
				else if(command.startsWith("create user") && PCSession.session.isConnected()) processCreateUser(command);
				else if(command.equals("connect")) processStandardConnect();
				else if(command.startsWith("connect")) {
					if(command.split(" ").length == 3) processConnect(command);
					else processConnectOLD(command);
				}
				else if(command.equals("login")) safeLogin();
				else if(command.startsWith("login")) processLogin(command);
				
				else System.out.println("Unknown command.");
			} else {
				// Commands that require the user to be logged in
				if(command.equals("ls debts")) processLsDebts();
				else if(command.equals("ls friends")) processLsFriends();
//				else if(command.startsWith("create updateListener")) processCreateUpdateListener(command);
				else if(command.startsWith("create debt")) processCreateDebt(command);
				else if(command.startsWith("accept debt") || command.startsWith("decline debt")) processAcceptDeclineCompleteDebt(command);
				else if(command.startsWith("complete debt")) processAcceptDeclineCompleteDebt(command);
				else if(command.startsWith("add friend")) processAddFriend(command);
				else if(command.startsWith("accept friend") || command.startsWith("decline friend")) processAcceptDeclineFriend(command);
				else if(command.equals("update")) processUpdate();
				else if(command.equals("timeto update")) processPrintTimeToUpdate();

				else System.out.println("Unknown command.");
			}
		}
		return false;
	}

	/**
	 * Starts a secure procedure for creating a new user.
	 * Will crash when not compiled (i.e. from an IDE)
	 */
	public static void safeCreateUser() {
		System.out.print("User name: ");
		try {
			String username = new BufferedReader(new InputStreamReader(System.in)).readLine();
			System.out.print("Password: ");
			String pw1 = new String(System.console().readPassword());
			System.out.print("Re-enter password: ");
			String pw2 = new String(System.console().readPassword());
			if(!pw1.equals(pw2)) {
				System.out.println("Your passwords does not match!");
			} else {
				processCreateUser("create user " + username + " " + pw1);
			}
		} catch (IOException e) {
			System.out.println("Input error! Please try again!");
		} catch (NullPointerException e) {
			System.out.println("Failed reading the password securely. Did you run this program from an IDE? Please try the normal version.");
		}
	}
	
	/**
	 * Starts a secure login procedure allowing requesting user name and password from the user in a secure form.
	 * Will crash used when not compiled (i.e. in an IDE)
	 */
	public static void safeLogin() {
		System.out.print("User name: ");
		try {
			String username = new BufferedReader(new InputStreamReader(System.in)).readLine();
			System.out.print("Password: ");
			String password = new String(System.console().readPassword());
			processLogin("login " + username + " " + password);
		} catch (IOException e) {
			System.out.println("Input error! Please try again.");
		} catch (NullPointerException e) {
			System.out.println("Failed reading the password securely. Did you run this program from an IDE? Please try the normal version.");
		}
	}
	
	public static void processPrintTimeToUpdate() {
		System.out.println("Time to next scheduled update:" + updater.getTimeToUpdate());
	}
	
	/**
	 * Force an update
	 */
	public static void processUpdate() {
		updater.update();
	}
	
	/**
	 * Starts the Updater with the given time between updates
	 * @param timeBetweenUpdates	The time between update requests
	 */
	public static void startUpdater(long timeBetweenUpdates) {
		updater.startUpdater(timeBetweenUpdates);
	}
	
	public static void processCreateUser(String command) {
		try {
			// Find username and password
			String username = command.split(" ")[2], password = command.split(" ")[3];
			if (username.length() > 30) {
				System.out.println("User name cannot exceed 30 characters. Please try again.");
				return;
			}
			PCSession.session.send(new CreateUserRequest(username, password).toXML());
			try {
				if(((CreateUserRequest) XMLSerializable.toObject(PCSession.session.receive())).isApproved()) System.out.println("User created.");
				else System.out.println("Could not create user.");
			} catch(IOException e) {
				printConnectionErrorMessage();
			}
		} catch (Exception e) {
			printSyntaxErrorMessage("create user <username> <password>");
		}
	}
	
	/**
	 * Process accepting/declining friend request
	 * Syntax: <accept/decline> friend <username>
	 * @param command	The command
	 */
	public static void processAcceptDeclineFriend(String command) {
		try {
			boolean accepted = command.split(" ")[0].equals("accept");
			// Find the entered username
			String username = command.split(" ")[2];
			// Find the corresponding friend request
			FriendRequest request = PCSession.session.getUser().getFriendRequestFrom(username);
			if(request == null) {
				System.out.println("You do not have any friend requests that match that username.");
				return;
			}
			// Update the status
			request.setStatus((accepted ? FriendRequestStatus.ACCEPTED : FriendRequestStatus.DECLINED));
			try {
				// Send the request to the server
//				PCSession.session.send(request.toXML());
				// Wait for response
//				FriendRequest response = (FriendRequest) XMLSerializable.toObject(PCSession.session.receive());
				FriendRequest response = (FriendRequest) XMLSerializable.toObject(Session.session.sendAndReceive(request.toXML()));
				if(response.getStatus() == request.getStatus()) System.out.println("Friend request " + (accepted ? "accepted" : "declined"));
				else {
					System.out.println("An error occurred! Please try again.");
					return;
				}
				// If we accepted the request, and the server processed it ok..
				if(response.getStatus() == FriendRequestStatus.ACCEPTED) {
					// Add the friend
					PCSession.session.getUser().addFriend(response.getFromUser());
					
				}
				// And remove the request since it has been answered
				PCSession.session.getUser().removeFriendRequest(request);
			} catch (IOException e) {
				// Reset status
				request.setStatus(FriendRequestStatus.PENDING);
				// Print error
				printConnectionErrorMessage();
			}
		} catch(Exception e) {
			printSyntaxErrorMessage("<accept/decline> friend <username>");
		}
	}
	
	/**
	 * Process the command "ls friends" by listing all friends in the console.
	 */
	public static void processLsFriends() {
		// Check if we have any friends
		if(PCSession.session.getUser().getNumberOfFriends() == 0) {
			System.out.println("You have no friends.\nTo add a new friend use the 'add friend <username>' command.");
		} else {
			// Print friends
			System.out.println("Your friends:");
			for (int i = 0; i < PCSession.session.getUser().getNumberOfFriends(); i++) {
				System.out.println(PCSession.session.getUser().getFriend(i).getUsername());
			}
		}
		// Print friend requests
		if(PCSession.session.getUser().getNumberOfFriendRequests() != 0) {
			System.out.println("\nYour pending friend requests:");
			for (int i = 0; i < PCSession.session.getUser().getNumberOfFriendRequests(); i++) {
				// Only print pending friend requests that are not outgoing
				FriendRequest r = PCSession.session.getUser().getFriendRequest(i);
				if(r.getStatus() == FriendRequestStatus.PENDING && r.getFriendUsername().equals(PCSession.session.getUser().getUsername()))
					System.out.println(r.getFromUser().getUsername());
			}
		}
	}
	
	/**
	 * Process the add friend command by sending a friend request to the server, which will be forwarded to the specified user.
	 * Syntax: "add friend <username>"
	 * @param command	The add friend command
	 */
	public static void processAddFriend(String command) {
		try {
			String friendUsername = command.split(" ")[2];
			// Check that the user is not sending a request to himself
			if(friendUsername.equals(PCSession.session.getUser().getUsername())) {
				System.out.println("You cannot send a friend request to yourself! What are you?!");
				return;
			}
			//Checking if the user already has a friend or a friend request with the requested user name
			User abb = PCSession.session.getUser();
			for (int i = 0; i<abb.getNumberOfFriends(); i++){
				if(friendUsername.equals(abb.getFriend(i).getUsername())){
					System.out.println("You are already friends with this user");
					return;
				}
			}
			// Send the friend request
			PCSession.session.send(new FriendRequest(friendUsername, PCSession.session.getUser()).toXML());
			try {
				FriendRequest response = (FriendRequest) XMLSerializable.toObject(PCSession.session.receive());
				switch(response.getStatus()) {
				case USER_NOT_FOUND:
					System.out.println("The user does not exist.");
					break;
				case UNHANDLED:
					System.err.println("Something wrong happened while sending your friend request. You should probably try again.");
					break;
				case ALREADY_EXISTS:
					// Check if this user already has a request from the requested friend
					String otherUsername = friendUsername;
					if(PCSession.session.getUser().hasFriendRequestFrom(otherUsername))
						System.out.println("You already have a friend request from that user.");
					else
						System.out.println("You have already sent a friend request to that user.");
					break;
				default:
					System.out.println("Friend request sent.");
				}
			} catch (IOException e) {
				printConnectionErrorMessage();
			}
		} catch (Exception e) {
			printSyntaxErrorMessage("add friend <username>");
		}
	}
	
	/**
	 * Process the given command as a accept debt, decline debt or complete debt.
	 * Command syntax: "<accept/decline/complete> debt"
	 * @param command	The command to process
	 */
	public static void processAcceptDeclineCompleteDebt(String command) {
		try {
			String[] cs = command.split(" ");
			String acceptOrDecline = cs[0];
			long id = Long.parseLong(cs[2]);
			Debt d = null;
			if(!acceptOrDecline.equals("complete")) {
				for (int i = 0; i < PCSession.session.getUser().getNumberOfPendingDebts(); i++) {
					// Find the debt with the specified ID, and check that this user is not the one that requested it
					if(PCSession.session.getUser().getPendingDebt(i).getId() == id && !PCSession.session.getUser().getPendingDebt(i).getRequestedBy().equals(PCSession.session.getUser())) {
						d = PCSession.session.getUser().getPendingDebt(i);
						break;
					}
				}
			} else {
				for (int i = 0; i < PCSession.session.getUser().getNumberOfConfirmedDebts(); i++) {
					if(PCSession.session.getUser().getConfirmedDebt(i).getId() == id) {
						d = PCSession.session.getUser().getConfirmedDebt(i);
						// Check if this user already has completed this debt
						if((d.getTo().equals(PCSession.session.getUser()) && d.getStatus() == DebtStatus.COMPLETED_BY_TO) || (d.getFrom().equals(PCSession.session.getUser()) && d.getStatus() == DebtStatus.COMPLETED_BY_FROM)) {
							System.out.println("You have already marked this debt as completed.");
							return;
						}
						break;
					}
				}
			}
			if(d == null) {
				System.out.println("You cannot " + acceptOrDecline + " that debt.");
				return;
			}
			if(acceptOrDecline.equals("accept")) {
				d.setStatus(DebtStatus.CONFIRMED);
			} else if(acceptOrDecline.equals("decline")) {
				d.setStatus(DebtStatus.DECLINED);
			} else {
				d.setStatus((d.getFrom().equals(PCSession.session.getUser()) ? DebtStatus.COMPLETED_BY_FROM : DebtStatus.COMPLETED_BY_TO));
			}
			
			try{
				PCSession.session.processUpdate(XMLSerializable.toObject(PCSession.session.sendAndReceive(d.toXML())));
			} catch (IOException e) {
				printConnectionErrorMessage();
			}
//			PCSession.session.send(d.toXML());
//			try{
//				PCSession.session.processUpdate(XMLSerializable.toObject(PCSession.session.receive()));
//			} catch (IOException e) {
//				printConnectionErrorMessage();
//			}
		} catch (Exception e) {
			printSyntaxErrorMessage("<accept/decline/complete> debt <ID>");
			System.out.println("Error: " + e);
		}
	}
	
	/**
	 * Prints a simple syntax error message with the correct syntax in System.out
	 * @param correctSyntax	The correct syntax
	 */
	public static void printSyntaxErrorMessage(String correctSyntax) {
		System.out.println("Syntax error!");
		if(correctSyntax != null) System.out.println("Correct stynax: " + correctSyntax);
	}
	
	/**
	 * Prints a simple connection error message in System.out
	 */
	public static void printConnectionErrorMessage() {
		System.out.println("An error occurred while communicating with the server. Please check your internet connection and try again.");
	}
	
	/**
	 * Process the given command as create debt
	 * Command syntax: "create debt <amount> "<what>" <to/from> "<to/from username>" "<comment>"
	 * @param command	The command to process
	 */
	public static void processCreateDebt(String command) {
		if(!PCSession.session.isLoggedIn()) {
			System.out.println("Please log in first.");
			return;
		}
		try {
			// Remove the two first spaces
			command = command.substring("create debt ".length());
			String[] cs = command.split('"' + "");
			double amount = Double.parseDouble(cs[0].trim());
			String what = cs[1], toFromUsername = cs[3], comment = cs[5], toFrom = cs[2].trim();
			if(!toFrom.equals("to") && !toFrom.equals("from")) throw new IllegalArgumentException("Must specify to or from");
			
			// TODO" Move functionality like this to PCSession or something, to make it reusable for GUI etc. also.
			User toFromUser = PCSession.session.getUser().getFriend(toFromUsername);
			if(toFromUser == null) {
				System.out.println("You can only create debts with your friends.");
				return;
			}
//			PCSession.session.send();
			try {
				Debt d = (Debt) XMLSerializable.toObject(Session.session.sendAndReceive(new Debt(-1, amount, what, (toFrom.equals("to") ? PCSession.session.getUser() : toFromUser), (toFrom.equals("to") ? toFromUser : PCSession.session.getUser()), comment, PCSession.session.getUser()).toXML()));
//				Debt d = (Debt)XMLSerializable.toObject(PCSession.session.receive());
				if(d.getId() != -1) {
					System.out.println("Debt created.");
					PCSession.session.processUpdate(d);
				} else System.out.println("An error occured when sending debt to server.");
			} catch(IOException e) {
				printConnectionErrorMessage();
			}
		} catch (Exception e) {
			printSyntaxErrorMessage("create debt <amount> " +'"' + "<what>" +'"' + " <to/from>" +'"' + "<to/from username>" +'"' +  +'"' + "<comment" +'"');
		}
	}
	
	/**
	 * @deprecated Is now automatically done with the connect command
	 * @param command	The command to process
	 */
//	public static void processCreateUpdateListener(String command) {
//		try {
//			new Thread(new UpdateListener(Integer.parseInt(command.split(" ")[2]))).start();
//		} catch (Exception e) {
//			printSyntaxErrorMessage("start updateListener <port>");
//		}
//	}
	
	/**
	 * Prints the specified number of tabs (white space) in System.out
	 * @param numberOfTabs	The number of tabs
	 */
	private static void printTabs(int numberOfTabs) {
		for (int i = 0; i < numberOfTabs; i++) {
			System.out.print("\t");
		}
	}
	
	/**
	 * Prints the given string with the specified amount space specified with tabs in System.out
	 * @param s		The string to print
	 * @param tabs	The total number of tabs (white space) the string + white space should fill
	 */
	private static void print(String s, int tabs) {
		System.out.print(s);
		printTabs(tabs - ((int) Math.ceil(s.length() / 8)));
	}
	
	/**
	 * Prints the given debts with the given title and the specified number of tabs for each column
	 * @param debts			The debts to print
	 * @param title			The title of this "table"
	 * @param numberOfTabs	The amount of space each column should be, specified in a number of tabs
	 */
	private static void printDebtsHelper(List<Debt> debts, String title, int[] numberOfTabs) {
		System.out.println(title + ":");
		print("ID", numberOfTabs[0]);
		print("Amount", numberOfTabs[1]);
		print("What", numberOfTabs[2]);
		print("To", numberOfTabs[3]);
		print("From", numberOfTabs[4]);
		print("Requested by", numberOfTabs[5]);
		System.out.print("Comment\n");
		for (Debt d : debts) {
			if(d.getStatus() == DebtStatus.COMPLETED) continue;
			print(""+d.getId(), numberOfTabs[0]);
			print(d.getAmount()+"", numberOfTabs[1]);
			print(d.getWhat(), numberOfTabs[2]);
			print(d.getTo().getUsername(), numberOfTabs[3]);
			print(d.getFrom().getUsername(), numberOfTabs[4]);
			print(d.getRequestedBy().getUsername(), numberOfTabs[5]);
			System.out.print(d.getComment());
			if(d.getStatus() == DebtStatus.COMPLETED_BY_FROM || d.getStatus() == DebtStatus.COMPLETED_BY_TO) {
				System.out.print("(Completed by " + (d.getStatus() == DebtStatus.COMPLETED_BY_FROM ? d.getFrom().getUsername() : d.getTo().getUsername()) + ")");
			}
			System.out.println();
		}
		if(debts.isEmpty()) System.out.println("None");
	}
	
	/**
	 * Prints the given debts with the given title
	 * @param debts		The debts
	 * @param listTitle	The title
	 */
	public static void printDebts(List<Debt> debts, String listTitle) {
		List<Debt> fromMe = new ArrayList<Debt>(), toMe = new ArrayList<Debt>();
		int[] maxChars = new int[6];
		for (Debt d : debts) {
			// Find the max lengths
			if((d.getId() + "").length() > maxChars[0]) maxChars[0] = (d.getId() + "").length();
			if((d.getAmount() + "").length() > maxChars[1]) maxChars[1] = (d.getAmount() + "").length();
			if(d.getWhat().length() > maxChars[2]) maxChars[2] = d.getWhat().length();
			if(d.getTo().getUsername().length() > maxChars[3]) maxChars[3] = d.getTo().getUsername().length();
			if(d.getFrom().getUsername().length() > maxChars[4]) maxChars[4] = d.getFrom().getUsername().length();
			if(d.getRequestedBy().getUsername().length() > maxChars[5]) maxChars[5] = d.getRequestedBy().getUsername().length();
			// Find receiver
			if(d.getTo().equals(PCSession.session.getUser())) toMe.add(d);
			else fromMe.add(d);
		}
		for (int i = 0; i < maxChars.length; i++) {
			maxChars[i] = (int) Math.ceil(maxChars[i] / 8.0); 
		}
		maxChars[5] = Math.max(maxChars[5], 2);	// Because "Requested by" is 12 characters (> 8) 
		printDebtsHelper(toMe, listTitle + " to me", maxChars);
		printDebtsHelper(fromMe, listTitle + " from me", maxChars);
	}
	
	/**
	 * Process the "ls debts" command by printing every debt this user is involved in
	 */
	public static void processLsDebts() {
		if(!PCSession.session.isLoggedIn()) System.out.println("Log in first.");
		else {
			printDebts(PCSession.session.getUser().getConfirmedDebts(), "Confirmed debts");
			printDebts(PCSession.session.getUser().getPendingDebts(), "Pending debts");
		}
	}
	
	public static void processLogin(String command) {
		if(!PCSession.session.isConnected()) {
			System.out.println("Please connect first.");
			return;
		}
		String username = null, password = null;
		try {
			username = command.split(" ")[1];
			password = command.split(" ")[2];
		} catch (Exception e) {
			printSyntaxErrorMessage("login <username> <password>");
		}
		// Attempt to log in
		switch(PCSession.session.logIn(username, password)) {
		case ACCEPTED:
			System.out.println("Log in ok.");
			Debugger.print("Starting updater.");
			startUpdater(Constants.STANDARD_TIME_BETWEEN_UPDATES);
			break;
		case ALREADY_LOGGED_ON:
			System.out.println("Logged in on another device.");
			break;
		case WRONG_INFORMATION:
			System.out.println("Wrong username or password.");
			break;
		case UNHANDLED:
			System.out.println("Something went wrong. Did your remember to connect first?");
			break;
		}
	}

	/**
	 * Process the given command as a connect command by connecting to the standard server.
	 * (Uses the processConnect-method to process the request.
	 * Syntax: "connect"
	 */
	public static void processStandardConnect() {
		processConnect("connect " + Constants.SERVER_ADDRESS + " " + Constants.STANDARD_SERVER_PORT);
	}
	
	/**
	 * Process the given command as a connect command by connecting to the specified server.
	 * Syntax: "connect <host> <port>"
	 * @param command
	 */
	public static void processConnect(String command) {
		try {
			String host = command.split(" ")[1];
			int port = Integer.parseInt(command.split(" ")[2]);
			PCSession.session.connect(host, port);
			if(PCSession.session.isConnected()) System.out.println("Connected.");
			else System.out.println("Could not connect to " + host + ":" + port);
		} catch (Exception e) {
			printSyntaxErrorMessage("connect <host> <port>");
		}
	}
	
	/**
	 * @deprecated or useful?
	 * Process the given command as a connect command. Will connect to the specified server and send a LogInRequest.
	 * Will also start a UpdateListener at the port specified in the command.
	 * Will also set the Sessions' user by calling it's logIn()-method.
	 * Syntax: "connect <username> <password> <host> <host port>"
	 * @param command	The command to process
	 */
	public static void processConnectOLD(String command) {
		try {
			String[] cs = command.split(" ");
			String username = cs[1], password = cs[2], host = cs[3];
			int port = Integer.parseInt(cs[4]);
			PCSession.session.connect(host, port);
			if(PCSession.session.isConnected()) {
				System.out.println("Connected.");
				LogInRequestStatus status = PCSession.session.logIn(username, password);
				if(status == LogInRequestStatus.ACCEPTED) {
					System.out.println("Logged in successfully.");
					// Start Updater
					startUpdater(Constants.STANDARD_TIME_BETWEEN_UPDATES);
				}
				else {
					System.out.println("Log in failed.");
				}
			} else {
				System.out.println("Connection failed.");
			}
		} catch(Exception e) {
			printSyntaxErrorMessage("connect <username> <password> <host> <port>");
		}
	}
}
