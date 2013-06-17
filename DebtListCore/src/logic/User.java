package logic;
import java.util.ArrayList;
import java.util.List;

import requests.FriendRequest;
import requests.xml.XMLSerializable;


public class User extends XMLSerializable {

	private boolean isOnline;
	
	/**
	 * Empty constructor for 
	 */
	public User() {}
	
	public User(long ID, String username) {
		this(ID, username, new ArrayList<User>());
	}
	
	public User(String username) {
		this(-1, username);
	}
	
	public User(long ID, String username, List<User> friends) {	
		setVariable("id", ID);
		setVariable("username", username);
		setVariable("pendingDebts", new ArrayList<Debt>());
		setVariable("confirmedDebts", new ArrayList<Debt>());
		setVariable("friendRequests", new ArrayList<FriendRequest>());
		setVariable("friends", friends);
	}

	public User(long ID, String username, String email, String activationKey, boolean isActivated) {	
		this(ID, username);
		setEmail(email);
		setActivationKey(activationKey);
		setIsActivated(isActivated);
	}
	
	/**
	 * User identification
	 * 
	 * @return
	 */
	public long getId() {
		return (Long) getVariable("id");
	}
	
	/**
	 * Sets the user's id
	 * @param id	The identification
	 */
	public void setId(long id) {
		setVariable("id", id);
	}

	/**
	 * Sets the user's activation key
	 * @param key	The activation key
	 */
	public void setActivationKey(String key) {
		setVariable("activationKey", key);
	}
	
	/**
	 * @return	The user's activation key
	 */
	public String getActivationKey() {
		return (String) getVariable("activationKey");
	}
	
	/**
	 * Determines if the user is activated or not
	 * @param isActivated
	 */
	public void setIsActivated(boolean isActivated) {
		setVariable("isActivated", isActivated);
	}
	
	/**
	 * @return	True if the user is activated, false if not
	 */
	public boolean isActivated() {
		return getVariable("isActivated") != null && (Boolean) getVariable("isActivated");
	}
	
	/**
	 * Sets the user's email
	 * @param email	The email
	 */
	public void setEmail(String email) {
		setVariable("email", email);
	}
	
	/**
	 * @return	The user's email or null if none
	 */
	public String getEmail() {
		return (String) getVariable("email");
	}
	
	/**
	 * Adds the given friend request to this user
	 * @param req	The friend request to add
	 */
	public synchronized void addFriendRequest(FriendRequest req) {
		((List<FriendRequest>) getVariable("friendRequests")).add(req);
	}
	
	/**
	 * Finds the given friend request's index
	 * @param req	The friend request
	 * @return		The index of the given friend request, or -1 if not found
	 */
	public synchronized int indexOfFriendRequest(FriendRequest req) {
		// We assume that only the user that can respond to the request has it saved... NO, that assumption is wrong!!
//		if(!req.getFriendUsername().equals(this.getUsername())) return -1;
		// Find the given request among ours
		for (int i = 0; i < this.getNumberOfFriendRequests(); i++) {
			if(req.getFromUser().equals(this.getFriendRequest(i).getFromUser())) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Checks if this user has the given friend request (recognized by fromUser and friendUsername).
	 * @param req	The friend request to check
	 * @return		True if this user has the given friend request, false if not
	 */
	public synchronized boolean hasFriendRequest(FriendRequest req) {
		return indexOfFriendRequest(req) != -1;
	}
	
	public synchronized boolean hasFriendRequestFrom(String username) {
		return hasFriendRequest(new FriendRequest(this.getUsername(), new User(username)));
	}
	
	/**
	 * Checks if this user has a friend request opposite of the one given as argument (recognized by fromUser and friendUsername).
	 * @param req	The friend request to check the opposite of
	 * @return		True if this user has the opposite friend request, false if not
	 */
	public synchronized boolean hasOppositeFriendRequest(FriendRequest req) {
		return hasFriendRequest(new FriendRequest(req.getFromUser().getUsername(), new User(req.getFriendUsername())));
	}
	
	/**
	 * Finds and returns the FriendRequest from the user with the given username
	 * @param username	The username of the user to look for
	 * @return			The corresponding friend request or null if none is found
	 */
	public synchronized FriendRequest getFriendRequestFrom(String username) {
		for (int i = 0; i < getNumberOfFriendRequests(); i++) {
			if(getFriendRequest(i).getFromUser().getUsername().equals(username)) {
				return getFriendRequest(i); 
			}
		}
		return null;
	}
	
	/**
	 * Removes the given friend request (recognized by fromUser and friendUsername)
	 * @param req	The friend request to remove
	 * @return		True if the request was present, false if not
	 */
	public synchronized boolean removeFriendRequest(FriendRequest req) {
		int i = indexOfFriendRequest(req);
		if(i == -1) return false;
		getFriendRequests().remove(i);
		return true;
	}
	
	/**
	 * @return	This user's friend requests
	 */
	private synchronized List<FriendRequest> getFriendRequests() {
		return (List<FriendRequest>) getVariable("friendRequests");
	}
	
	/**
	 * @param i	The index
	 * @return	The ith friend request
	 */
	public synchronized FriendRequest getFriendRequest(int i) {
		return getFriendRequests().get(i);
	}
	
	/**
	 * @return	The number of pending friend requests
	 */
	public synchronized int getNumberOfFriendRequests() {
		return getFriendRequests().size();
	}
	
	/**
	 * Returns a sendable VERSION of this object (avoiding infinite loops with friends list for example).
	 * @param fromServer	If the returned object will be sent from the server (true) or not (false). If false debts will not be included.
	 * @return				A sendable VERSION of this object.
	 */
	/*public Sendable toSendable(boolean fromServer) {
		List<String> friendUsernames = new ArrayList<String>();
		if(friends != null) {
			for (User f : friends) {
				friendUsernames.add(f.getUsername());
			}
		}
		List<Debt> pd = new ArrayList<Debt>();
		List<Debt> cd = new ArrayList<Debt>();
		if(fromServer) {
			for (Debt d : pendingDebts) {
				pd.add((Debt) d.toSendable(false));
//				pd.add(new Debt(d.getId(), d.getAmount(), d.getWhat(), from, to, comment, requestedBy))
			}
			for (Debt d : confirmedDebts) {
				cd.add((Debt) d.toSendable(false));
			}
		}
		return new User(username, password, friendUsernames, (fromServer ? pd : null), (fromServer ? cd : null));
	}*/
	
	public synchronized List<Debt> getPendingDebts() {
		return (List<Debt>) getVariable("pendingDebts");
	}
	
	public synchronized List<Debt> getConfirmedDebts() {
		return (List<Debt>) getVariable("confirmedDebts");
	}
	
	/**
	 * Removes the given pending debt based on it's id
	 * @param d	The debt to remove
	 * @return	True if the debt was removed, false if it was not present
	 */
	public synchronized boolean removePendingDebt(Debt d) {
		for (int i = 0; i < getNumberOfPendingDebts(); i++) {
			if(getPendingDebt(i).getId() == d.getId()) {
				removePendingDebt(i);
				return true;
			}
		}
		return false;
	}
	
	public synchronized int getNumberOfWaitingDebts() {
		int c = 0;
		for (Debt d : getPendingDebts()) {
			if(d.getRequestedBy() != this) c++;
		}
		return c;
	}
	
	public synchronized int getNumberOfPendingDebts() {
		return getPendingDebts().size();
	}
	
	public synchronized int getNumberOfConfirmedDebts() {
		return getConfirmedDebts().size();
	}
	
	public synchronized Debt getPendingDebt(int i) {
		return getPendingDebts().get(i);
	}
	
	public synchronized Debt getConfirmedDebt(int i) {
		return getConfirmedDebts().get(i);
	}
	
	public synchronized void addPendingDebt(Debt d) {
		getPendingDebts().add(d);
	}
	
	public synchronized void addConfirmedDebt(Debt d) {
		getConfirmedDebts().add(d);
	}
	
	public synchronized int getNumberOfTotalDebts() {
		return getNumberOfPendingDebts() + getNumberOfConfirmedDebts();
	}

	public synchronized User getFriend(String username) {
		for (int i = 0; i < getNumberOfFriends(); i++) {
			if(getFriend(i).getUsername().equalsIgnoreCase(username)) return getFriend(i);
		}
		return null;
	}
	
	private synchronized List<User> getFriends() {
		return (List<User>) getVariable("friends");
	}
	
	public synchronized int getNumberOfFriends() {
		return getFriends().size();
	}
	
	public synchronized User getFriend(int i) {
		return getFriends().get(i);
	}
	
	public String getUsername() {
		return (String) getVariable("username");
	}
	
	public boolean isOnline() {
		return isOnline;
	}
	
	public void setIsOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public synchronized Debt removeConfirmedDebt(int i) {
		return getConfirmedDebts().remove(i);
	}
	
	/**
	 * Removes the given debt based on it's id.
	 * 
	 * @param d	The debt to remove
	 * @return	True if the debt was removed, false if it was not present
	 */
	public synchronized boolean removeConfirmedDebt(Debt d) {
		for (int i = 0; i < getNumberOfConfirmedDebts(); i++) {
			if(getConfirmedDebt(i).getId() == d.getId()) {
				removeConfirmedDebt(i);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the confirmed debt with the id given as argument, or null if no match was found
	 * @param id	The id to look for
	 * @return		The debt with the corresponding id, or null if none was found
	 */
	public synchronized Debt getConfirmedDebtById(long id) {
		for (int i = 0; i < getNumberOfConfirmedDebts(); i++) {
			if(getConfirmedDebt(i).getId() == id) {
				return getConfirmedDebt(i);
			}
		}
		return null;
	}
	
	public synchronized Debt removePendingDebt(int i) {
		return getPendingDebts().remove(i);
	}
	
	public synchronized void addFriend(User friend) {
		if(getFriends() == null) {
			setVariable("friends", new ArrayList<User>());
		}
		getFriends().add(friend);
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof User)) return false;
		return getUsername().equalsIgnoreCase(((User)o).getUsername());
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Username: " + getUsername());
//		if(getFriends() != null) {
//			sb.append(", friends = [");
//			for (int i = 0; i < getFriends().size(); i++) {
//				User f = getFriends().get(i);
//				sb.append(f.toString());
//				if(i < getFriends().size() - 1) {
//					sb.append(", ");
//				}
//			}
//			sb.append("]");
//		}
		return sb.toString();
	}
}
