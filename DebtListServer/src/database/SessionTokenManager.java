package database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import requests.UpdateRequest;

import utils.PasswordHasher;

import network.ServerConnectionHandler;

/**
 *	A class that generates and keeps track of tokens, and their associated ServerConnectionHandlers 
 */
public class SessionTokenManager {

	// Sessions are no longer associated with handlers (but updates isntead) as of 16th of June 2013
//	private Map<String, ServerConnectionHandler> handlers;
	private Map<String, String> usernames;	// <token, username>
	private Map<String, UpdateRequest> updates; // <token, update>
	
	public SessionTokenManager() {
//		handlers = new HashMap<String, ServerConnectionHandler>();
		usernames = new HashMap<String, String>();
		updates = new HashMap<String, UpdateRequest>();
	}
	
	/**
	 * Generates a token, and stores the token and the given ServerConnectionHandler as a key-value pair
	 * @param handler	The ServerConnectionHandler to store with the token
	 * @return			The generated token
	 */
	public String generateToken(ServerConnectionHandler handler) {
		String token = PasswordHasher.hashPassword((System.currentTimeMillis() + (long) (Math.random() * 10000000)) + "");
		registerToken(token, handler);
		return token;
	}
	
	/**
	 * Registers the given token-handler pair
	 * @param token
	 * @param handler
	 */
	public void registerToken(String token, ServerConnectionHandler handler) {
//		handlers.put(token, handler);
		System.out.println("TokenManager: Registering token: " + token);
		usernames.put(token, handler.getUser().getUsername());
		updates.put(token, new UpdateRequest());
	}
	
	/**
	 * Returns the username associated with the given token, or null if none
	 * 
	 * @param token
	 * @return
	 */
	public String getUsername(String token) {
		return usernames.get(token);
	}
	
	/**
	 * Returns the update request associated with the given token
	 * 
	 * @param token
	 * @return
	 */
	public UpdateRequest getUpdate(String token) {
		return updates.get(token);
	}
	
	/**
	 * Returns all the update requests associated with the given user name
	 * 
	 * @param username
	 * @return
	 */
	public List<UpdateRequest> getUpdates(String username) {
		List<UpdateRequest> l = new ArrayList<UpdateRequest>();
		for (String t : usernames.keySet()) {
			if(usernames.get(t).equalsIgnoreCase(username)) {
				l.add(updates.get(t));
			}
		}
		return l;
	}
	
	/**
	 * Returns the value (ServerConnectionHandler) associated with the given key (token), or null if none
	 * 
	 * @param token
	 * @return
	 */
//	public ServerConnectionHandler getHandler(String token) {
//		return handlers.get(token);
//	}
	
	/**
	 * Removes the key-value (token-ServerConnectionHandler) pair identified by the key (token)
	 * @param token	The pair's token
	 * @return		The previous value (ServerConnectionHandler) associated with the key (token), or null if none
	 */
//	public ServerConnectionHandler remove(String token) {
//		return handlers.remove(token);
//	}
	
	
}
