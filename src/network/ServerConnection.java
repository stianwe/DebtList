package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import database.DatabaseUnit;

import logic.Debt;
import logic.DebtStatus;
import logic.User;
import requests.FriendRequest;
import requests.FriendRequest.FriendRequestStatus;
import requests.xml.XMLSerializable;
import utils.PasswordHasher;

public class ServerConnection {

	private DatabaseUnit dbUnit;
	private Map<String, User> users;
	private Map<String, String> passwords;
	private List<ServerConnectionHandler> handlers;
	private long nextDebtId, nextUserId, nextFriendRequestId;
	
	public ServerConnection() {
		this.handlers = new ArrayList<ServerConnectionHandler>();
		dbUnit = new DatabaseUnit();
		dbUnit.connect();
		users = new HashMap<String, User>();
		passwords = new HashMap<String, String>();
		nextDebtId = 0; nextUserId = 0; nextFriendRequestId = 0;
		try {
			for(Map.Entry<User, String> entry :	dbUnit.loadUsers().entrySet()) {
				users.put(entry.getKey().getUsername(), entry.getKey());
				passwords.put(entry.getKey().getUsername(), entry.getValue());
			}
			dbUnit.loadFriends(users);
			dbUnit.loadDebts(users);
			new Timer().schedule(new TimerTask() {
				
				@Override
				public void run() {
					try {
						saveAll();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						System.out.println("Failed writing to database!");
						e.printStackTrace();
					}
				}
			}, Constants.TIME_BETWEEN_WRITES_TO_DATABASE, Constants.TIME_BETWEEN_WRITES_TO_DATABASE);
		} catch (SQLException e) {
			System.out.println("FAILED TO LOAD!");
			e.printStackTrace();
		}
	}
	
	public synchronized void saveAll() throws SQLException {
		dbUnit.save(users.values(), passwords);
	}
	
	public synchronized void saveUser(User u) throws SQLException {
		List<User> user = new ArrayList<User>();
		user.add(u);
		Map<String, String> pw = new HashMap<String, String>();
		pw.put(u.getUsername(), passwords.get(u.getUsername()));
		dbUnit.save(user, passwords);
	}
	
	public synchronized void addPassword(String username, String password) {
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
	 * @return	The next available user id
	 */
	public synchronized long getNextUserId() {
		return nextUserId++;
	}
	
	/**
	 * @return	The next available friend request id
	 */
	public synchronized long getNextFriendRequestId() {
		return nextFriendRequestId++;
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
			handler.sendUpdate(objectToSend);
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
		passwords.put(user.getUsername(), PasswordHasher.hashPassword(password));
	}
	
	synchronized public boolean checkPassword(User user, String password) {
		return passwords.containsKey(user.getUsername()) 
				&& passwords.get(user.getUsername()).equals(password);
	}
	
	public static void main(String[] args) {
		ServerConnection server = new ServerConnection();
		
		// START TEST DATA
//		User arne = new User(1, "arnegopro");
//		User stian = new User(2, "stian");
//		User test = new User(0, "test");
//		// All friends must also have a corresponding friend request to work with the database!
//		stian.addFriendRequest(new FriendRequest(stian.getUsername(), test, FriendRequestStatus.PENDING, 0));
//		stian.addFriendRequest(new FriendRequest(stian.getUsername(), arne, FriendRequestStatus.ACCEPTED, 1));
//		stian.addFriend(arne);
//		arne.addFriend(stian);
//		server.addUser(arne, "qazqaz");
//		server.addUser(stian, "asd");
//		server.addUser(test, "test");
//		Debt d1 = new Debt(0, 15, "kr", stian, arne, "Tralalala", stian, DebtStatus.CONFIRMED);
//		stian.addConfirmedDebt(d1);
//		arne.addConfirmedDebt(d1);
//		Debt d2 = new Debt(1, 7, "kr", arne, stian, "Tralalla2", arne, DebtStatus.REQUESTED);
//		stian.addPendingDebt(d2);
//		arne.addPendingDebt(d2);
//		server.nextDebtId = 2;
//		server.nextFriendRequestId =  2;
//		server.nextUserId = 3;
		// END TEST DATA
		
		// Print loaded users on startup
		System.out.println("Loaded users:");
		for (String s : server.users.keySet()) {
			System.out.println(s);
		}
		
		// Accept connections on port 13337
		server.accept(13337);
	}
}