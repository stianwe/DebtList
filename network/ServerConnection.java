package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logic.User;

public class ServerConnection {

	private Map<String, User> users;
	private List<ServerConnectionHandler> handlers;
	
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
		server.users.put("arnegopro", new User("arnegopro", "qazqaz"));
		server.accept(13337);
	}
}