package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logic.Debt;
import logic.DebtStatus;
import logic.User;
import requests.xml.XMLSerializable;

public class ServerConnection {

	private Map<String, User> users;
	private Map<String, String> passwords;
	private List<ServerConnectionHandler> handlers;
	private long nextDebtId;
	
	public ServerConnection() {
		this.handlers = new ArrayList<ServerConnectionHandler>();
		users = new HashMap<String, User>();
	}
	
	public void addPassword(String username, String password) {
		passwords.put(username, password);
	}
	
	public synchronized void addConnectionHandler(ServerConnectionHandler handler) {
		this.handlers.add(handler);
	}
	
	public synchronized void removeConnectionHandler(ServerConnectionHandler handler) {
		this.handlers.remove(handler);
	}
	
	/**
	 * @return	The next available debt id
	 */
	public synchronized long getNextDebtId() {
		return nextDebtId++;
	}

	/**
	 * Notifies the specified user by sending the given object to the user's UpdateListener
	 * @param username		The user to notify
	 * @param objectToSend	The object to send
	 */
	public void notifyUser(String username, XMLSerializable objectToSend) {
		System.out.println("Notifying " + username);
		ServerConnectionHandler handler = getHandler(username);
		if(handler != null) {
			handler.sendUpdate(objectToSend.toXML());
			System.out.println("Sent to: " + handler.getUser().getUsername());
		}
	}
	
	/**
	 * Returns the specified user's ServerConnectionHandler
	 * @param username	The user's user name
	 * @return			The user's ServerConnectionHandler
	 */
	public ServerConnectionHandler getHandler(String username) {
		for (ServerConnectionHandler h : handlers) {
			if(h.getUser().getUsername().equals(username)) return h;
		}
		return null;
	}
	
	/**
	 * Listens at the specified port for incoming connections.
	 * Incoming connections are given to a ServerConnectionHandler that is started in a separate Thread.
	 * This method will run "forever"
	 * @param port						The port to listen to
	 */
	public void accept(int port) {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
			while(true) {
				System.out.println("Listening for incomming connections..");
				new ServerConnectionHandler(ss.accept(), this).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				ss.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Returns the user with the given username, or null if no user is found.
	 * @param username	The username
	 * @return			The user or null
	 */
	synchronized public User getUser(String username) {
		return users.get(username);
	}
	
	synchronized public void addUser(User user, String password) {
		users.put(user.getUsername(), user);
		passwords.put(user.getUsername(), password);
	}
	
	public static void main(String[] args) {
		ServerConnection server = new ServerConnection();
		server.nextDebtId = 0;
		User arne = new User(1, "arnegopro");
		User stian = new User(2, "stian");
		stian.addFriend(arne);
		arne.addFriend(stian);
		server.addUser(arne, "qazqaz");
		server.addUser(stian, "asd");
		System.out.println("Loaded users:");
		for (String s : server.users.keySet()) {
			System.out.println(s);
		}
		
		// TODO: TEST IF LOADED DEBTS IS SENT
		Debt d1 = new Debt(server.getNextDebtId(), 100, "g", arne, stian, "goldz", stian);
		Debt d2 = new Debt(server.getNextDebtId(), 12, "s", stian, arne, "s", stian);
		Debt d3 = new Debt(server.getNextDebtId(), 1337, "slaps", stian, arne, ":D", arne);
		Debt d4 = new Debt(server.getNextDebtId(), 42, "42ere", arne, stian, "haha", arne);
		d4.setStatus(DebtStatus.CONFIRMED);
		stian.addPendingDebt(d1);
		stian.addPendingDebt(d2);
		stian.addPendingDebt(d3);
		stian.addConfirmedDebt(d4);
		arne.addPendingDebt(d1);
		arne.addPendingDebt(d2);
		arne.addPendingDebt(d3);
		arne.addConfirmedDebt(d4);
		
		server.accept(13337);
	}
}