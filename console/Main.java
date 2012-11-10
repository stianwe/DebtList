package console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import logic.Debt;
import logic.DebtStatus;
import logic.User;
import requests.LogInRequestStatus;
import requests.UpdateListener;
import requests.xml.XMLSerializable;
import session.Session;

public class Main {

	public static void main(String[] args) {
		System.out.println("Welcome to DebtList (version 0)!");
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
	}
	
	/**
	 * Processes the given command.
	 * @param command	The command to process
	 * @return			True if the command was "exit", false if not
	 */
	public static boolean processCommand(String command) {
		if(command.equals("exit")) return true;
		else if(command.startsWith("connect")) processConnect(command);
		else if(command.equals("ls debts")) processLsDebts();
		else if(command.startsWith("create updateListener")) processCreateUpdateListener(command);
		else if(command.startsWith("create debt")) processCreateDebt(command);
		else if(command.startsWith("accept debt") || command.startsWith("decline debt")) processAcceptDeclineCompleteDebt(command);
		else if(command.startsWith("complete debt")) processAcceptDeclineCompleteDebt(command);
		
		else System.out.println("Unknown command.");
		return false;
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
				for (int i = 0; i < Session.session.getUser().getNumberOfPendingDebts(); i++) {
					// Find the debt with the specified ID, and check that this user is not the one that requested it
					if(Session.session.getUser().getPendingDebt(i).getId() == id && !Session.session.getUser().getPendingDebt(i).getRequestedBy().equals(Session.session.getUser())) {
						d = Session.session.getUser().getPendingDebt(i);
						break;
					}
				}
			} else {
				for (int i = 0; i < Session.session.getUser().getNumberOfConfirmedDebts(); i++) {
					if(Session.session.getUser().getConfirmedDebt(i).getId() == id) {
						d = Session.session.getUser().getConfirmedDebt(i);
						// Check if this user already has completed this debt
						if((d.getTo().equals(Session.session.getUser()) && d.getStatus() == DebtStatus.COMPLETED_BY_TO) || (d.getFrom().equals(Session.session.getUser()) && d.getStatus() == DebtStatus.COMPLETED_BY_FROM)) {
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
				d.setStatus((d.getFrom().equals(Session.session.getUser()) ? DebtStatus.COMPLETED_BY_FROM : DebtStatus.COMPLETED_BY_TO));
			}
			
			Session.session.send(d.toXML());
			Session.session.processUpdate(XMLSerializable.toObject(Session.session.receive()));
			// TODO	What will we receive?

		} catch (Exception e) {
			System.out.println("Syntax error!");
			System.out.println("Correct syntax: <accept/decline/complete> debt <ID>");
		}
	}
	
	/**
	 * Process the given command as create debt
	 * Command syntax: "create debt <amount> "<what>" <to/from> "<to/from username>" "<comment>"
	 * @param command	The command to process
	 */
	public static void processCreateDebt(String command) {
		if(!Session.session.isLoggedIn()) {
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
			
			// TODO" Move functionality like this to Session or something, to make it reusable for GUI etc. also.
			User toFromUser = Session.session.getUser().getFriend(toFromUsername);
			if(toFromUser == null) {
				System.out.println("You can only create debts with your friends.");
				return;
			}
			Session.session.send(new Debt(-1, amount, what, (toFrom.equals("to") ? Session.session.getUser() : toFromUser), (toFrom.equals("to") ? toFromUser : Session.session.getUser()), comment, Session.session.getUser()).toXML());
			Debt d = (Debt)XMLSerializable.toObject(Session.session.receive());
			if(d.getId() != -1) {
				System.out.println("Debt created.");
				Session.session.processUpdate(d);
			} else System.out.println("An error occured when sending debt to server.");
		} catch (Exception e) {
			System.out.println("Syntax error!");
			System.out.println("Correct syntax: create debt <amount> " +'"' + "<what>" +'"' + " <to/from>" +'"' + "<to/from username>" +'"' +  +'"' + "<comment" +'"');
			e.printStackTrace();
		}
	}
	
	/**
	 * @deprecated Is now automatically done with the connect command
	 * @param command	The command to process
	 */
	public static void processCreateUpdateListener(String command) {
		try {
			new Thread(new UpdateListener(Integer.parseInt(command.split(" ")[2]))).start();
		} catch (Exception e) {
			System.out.println("Syntax error!");
			System.out.println("Correct syntax: start updateListener <port>");
		}
	}
	
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
			if(d.getTo().equals(Session.session.getUser())) toMe.add(d);
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
		if(!Session.session.isLoggedIn()) System.out.println("Log in first.");
		else {
			printDebts(Session.session.getUser().getConfirmedDebts(), "Confirmed debts");
			printDebts(Session.session.getUser().getPendingDebts(), "Pending debts");
		}
	}
	
	/**
	 * Process the given command as a connect command. Will connect to the specified server and send a LogInRequest.
	 * Will also start a UpdateListener at the port specified in the command.
	 * Will also set the Sessions' user by calling it's logIn()-method.
	 * Syntax: "connect <username> <password> <host> <host port> <UpdateListener port>"
	 * @param command	The command to process
	 */
	public static void processConnect(String command) {
		try {
			String[] cs = command.split(" ");
			String username = cs[1], password = cs[2], host = cs[3];
			int port = Integer.parseInt(cs[4]), updatePort = Integer.parseInt(cs[5]);
			Session.session.connect(host, port);
			Thread t = null;
			if(Session.session.isConnected()) {
				System.out.println("Connected.");
				t = new Thread(new UpdateListener(updatePort));
				t.start();
				System.out.println("Update listener started on port " + updatePort);
				LogInRequestStatus status = Session.session.logIn(username, password, updatePort);
				if(status == LogInRequestStatus.ACCEPTED) {
					System.out.println("Logged in successfully.");
				}
				else {
					System.out.println("Log in failed.");
					System.out.println("Trying to kill UpdateListener");
					t.interrupt();
				}
			} else {
				System.out.println("Connection failed.");
			}
		} catch(Exception e) {
			System.out.println("Syntax error!");
			System.out.println("Correct syntax: connect <username> <password> <host> <port>");
			e.printStackTrace();
		}
	}
}
