package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import requests.XMLParsable;

import logic.Debt;
import logic.User;

public class ServerConnection {

	private Map<String, User> users;
	private List<ServerConnectionHandler> handlers;
	private long nextDebtId;
	
	public ServerConnection() {
		this.handlers = new ArrayList<ServerConnectionHandler>();
		users = new HashMap<String, User>();
	}
	
	public synchronized void addConnectionHandler(ServerConnectionHandler handler) {
		this.handlers.add(handler);
	}
	
	public synchronized void removeConnectionHandler(ServerConnectionHandler handler) {
		this.handlers.remove(handler);
	}
	
	public synchronized long getNextDebtId() {
		return nextDebtId++;
	}
	
	public void notifyUser(String username, XMLParsable objectToSend) {
		ServerConnectionHandler handler = getHandler(username);
		if(handler != null) handler.sendUpdate(objectToSend.toXml());
	}
	
	public ServerConnectionHandler getHandler(String username) {
		for (ServerConnectionHandler h : handlers) {
			if(h.getUser().getUsername().equals(username)) return h;
		}
		return null;
	}
	
	public void accept(int port) throws IllegalArgumentException {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
			while(true) {
				System.out.println("Listening for incomming connections..");
				new ServerConnectionHandler(ss.accept(), this).start();
				System.out.println("Someone connected!");
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
	
	synchronized public User getUser(String username) {
		return users.get(username);
	}
	
	synchronized public void addUser(User user) {
		users.put(user.getUsername(), user);
	}
	
	public static void main(String[] args) {
		ServerConnection server = new ServerConnection();
		server.nextDebtId = 1;
		User arne = new User("arnegopro", "qazqaz");
		User stian = new User("stian", "asd");
		stian.addFriend(arne);
		arne.addFriend(stian);
		server.users.put("arnegopro", arne);
		server.users.put("stian", stian);
		System.out.println("Loaded users:");
		for (String s : server.users.keySet()) {
			System.out.println(s);
		}
		
		// TODO: TEST IF LOADED DEBTS IS SENT
		Debt d1 = new Debt(0, 100, "g", arne, stian, "goldz", stian);
		Debt d2 = new Debt(1, 12, "s", stian, arne, "s", stian);
		Debt d3 = new Debt(2, 1337, "slaps", stian, arne, ":D", arne);
		Debt d4 = new Debt(2, 42, "42ere", arne, stian, "haha", arne);
		d4.setIsConfirmed(true);
		stian.addPendingDebt(d1);
		stian.addPendingDebt(d2);
		stian.addPendingDebt(d3);
		stian.addConfirmedDebt(d4);
		arne.addPendingDebt(d1);
		arne.addPendingDebt(d2);
		arne.addPendingDebt(d3);
		arne.addConfirmedDebt(d4);
		
//		ServerConnectionHandler h = new ServerConnectionHandler(new Socket(), server);
//		h.setUser(stian);
//		h.processDebt(new Debt(-1, 100, "testers", stian, (User) arne.toSendable(), "testing", stian));
		server.accept(13337);
	}
}