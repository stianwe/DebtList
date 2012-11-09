package logic;
import java.util.ArrayList;
import java.util.List;

import requests.xml.XMLSerializable;


public class User extends XMLSerializable {

//	private DebtList debts;
	private boolean isOnline;
	
	/**
	 * Empty constructor for 
	 */
	public User() {}
	
	public User(long ID, String username) {
		this(ID, username, null);
	}
	
	public User(String username, String password) {
		this(-1, username, password);
	}
	
	public User(long ID, String username, String password) {
		this(ID, username, password, null);
	}	
	
	public User(long ID, String username, String password, List<User> friends) {	
		setVariable("id", ID);
		// TODO: Add all variables to the list!
		setVariable("username", username);
		setVariable("password", password);
		// TODO: FIX UNDER!
//		debts = new DebtList(this, null);
		setVariable("pendingDebts", new ArrayList<Debt>());
		setVariable("confirmedDebts", new ArrayList<Debt>());
		setVariable("friends", friends);
	}
	
	// ?
	/*private User(String username, String password, List<String> friendUsernames, List<Debt> pendingDebts, List<Debt> confirmedDebts) {
		this(username, password);
		for (String s : friendUsernames) {
			addFriend(new User(s));
		}
		setVariable("pendingDebts", pendingDebts);
		setVariable("confirmedDebts", confirmedDebts);
	}*/
	
	/**
	 * User identification
	 * 
	 * @return
	 */
	public long getId() {
		return (Long) getVariable("id");
	}
	
	/**
	 * Returns a sendable version of this object (avoiding infinite loops with friends list for example).
	 * @param fromServer	If the returned object will be sent from the server (true) or not (false). If false debts will not be included.
	 * @return				A sendable version of this object.
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
	
	public List<Debt> getPendingDebts() {
		return (List<Debt>) getVariable("pendingDebts");
	}
	
	public List<Debt> getConfirmedDebts() {
		return (List<Debt>) getVariable("confirmedDebts");
	}
	
	public boolean removePendingDebt(Debt d) {
		return pendingDebts.remove(d);
	}
	
	public int getNumberOfWaitingDebts() {
		int c = 0;
		for (Debt d : getPendingDebts()) {
			if(d.getRequestedBy() != this) c++;
		}
		return c;
	}
	
	public int getNumberOfPendingDebts() {
		return getPendingDebts().size();
	}
	
	public int getNumberOfConfirmedDebts() {
		return getConfirmedDebts().size();
	}
	
	public Debt getPendingDebt(int i) {
		return getPendingDebts().get(i);
	}
	
	public Debt getConfirmedDebt(int i) {
		return getConfirmedDebts().get(i);
	}
	
	public void addPendingDebt(Debt d) {
		getPendingDebts().add(d);
	}
	
	public void addConfirmedDebt(Debt d) {
		getConfirmedDebts().add(d);
	}
	
	public int getNumberOfTotalDebts() {
		return getNumberOfPendingDebts() + getNumberOfConfirmedDebts();
	}
	
//	public void addDebt(Debt d) {
//		debts.addPendingDebt(d);
//		pendingDebts.add(d);
//	}
	
//	public DebtList getDebts() {
//		return debts;
//	}

	public User getFriend(String username) {
		for (int i = 0; i < getNumberOfFriends(); i++) {
			if(getFriend(i).getUsername().equalsIgnoreCase(username)) return getFriend(i);
		}
		return null;
	}
	
	private List<User> getFriends() {
		return (List<User>) getVariable("friends");
	}
	
	public int getNumberOfFriends() {
		return getFriends().size();
	}
	
	public User getFriend(int i) {
		return getFriends().get(i);
	}
	
	public String getUsername() {
		return (String) getVariable("username");
	}
	
	public String getPassword() {
		return (String) getVariable("password");
	}
	
	public boolean isOnline() {
		return isOnline;
	}
	
	public void setIsOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public Debt removeConfirmedDebt(int i) {
		return getConfirmedDebts().remove(i);
	}
	
	public Debt removePendingDebt(int i) {
		return getPendingDebts().remove(i);
	}
	
	public void addFriend(User friend) {
		if(getFriends() == null) {
			setVariable("friends", new ArrayList<User>());
		}
		getFriends().add(friend);
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof User)) return false;
		return getUsername().equals(((User)o).getUsername());
//		User u = (User) o;
//		if(this.getNumberOfVariables() != u.getNumberOfVariables()) return false;
//		for (int i = 0; i < this.getNumberOfVariables(); i++) {
//			if(!u.getVariable(this.getVariableName(i)).equals(this.getVariable(i))) return false;
//		}
//		return true;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Username: " + getUsername() + ", password: " + getPassword());
		if(getFriends() != null) {
			sb.append(", friends = [");
			for (int i = 0; i < getFriends().size(); i++) {
				User f = getFriends().get(i);
				sb.append(f.toString());
				if(i < getFriends().size() - 1) {
					sb.append(", ");
				}
			}
			sb.append("]");
		}
		return sb.toString();
	}
}
