package logic;
import java.util.ArrayList;
import java.util.List;

import requests.XMLParsable;


public class User extends Sendable{

	private String username, password, surName, lastName;
	private List<User> friends;
	private List<Debt> pendingDebts, confirmedDebts;
//	private DebtList debts;
	private boolean isOnline;
	
	public User(String username, String password) {
		// TODO: Add all variables to the list!
		this.username = username;
		addVariable("username", this.username);
		this.password = password;
		addVariable("password", this.password);
		// TODO: FIX UNDER!
//		debts = new DebtList(this, null);
		pendingDebts = new ArrayList<Debt>();
		addVariable("pendingDebts", pendingDebts);
		confirmedDebts = new ArrayList<Debt>();
		addVariable("confirmedDebts", confirmedDebts);
	}
	
	public User(String username) {
		this(username, null);
	}
	
	public User(String username, String password, List<User> friends) {
		this(username, password);
		this.friends = friends;
		addVariable("friends", friends);
	}
	
	private User(String username, String password, List<String> friendUsernames, List<Debt> pendingDebts, List<Debt> confirmedDebts) {
		this(username, password);
		for (String s : friendUsernames) {
			addFriend(new User(s));
		}
		addVariable("pendingDebts", pendingDebts);
		addVariable("confirmedDebts", confirmedDebts);
	}
	
	/**
	 * Returns a sendable version of this object (avoiding infinite loops with friends list for example).
	 * @param fromServer	If the returned object will be sent from the server (true) or not (false). If false debts will not be included.
	 * @return				A sendable version of this object.
	 */
	public Sendable toSendable(boolean fromServer) {
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
	}
	
	public List<Debt> getPendingDebts() {
		return pendingDebts;
	}
	
	public List<Debt> getConfirmedDebts() {
		return confirmedDebts;
	}
	
	public boolean removePendingDebt(Debt d) {
		return pendingDebts.remove(d);
	}
	
	public int getNumberOfWaitingDebts() {
		int c = 0;
		for (Debt d : pendingDebts) {
			if(d.getRequestedBy() != this) c++;
		}
		return c;
	}
	
	public int getNumberOfPendingDebts() {
		return pendingDebts.size();
	}
	
	public int getNumberOfConfirmedDebts() {
		return confirmedDebts.size();
	}
	
	public Debt getPendingDebt(int i) {
		return pendingDebts.get(i);
	}
	
	public Debt getConfirmedDebt(int i) {
		return confirmedDebts.get(i);
	}
	
	public void addPendingDebt(Debt d) {
		pendingDebts.add(d);
	}
	
	public void addConfirmedDebt(Debt d) {
		confirmedDebts.add(d);
	}
	
	public int getNumberOfTotalDebts() {
		return pendingDebts.size() + confirmedDebts.size();
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
	
	public int getNumberOfFriends() {
		return friends.size();
	}
	
	public User getFriend(int i) {
		return friends.get(i);
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public boolean isOnline() {
		return isOnline;
	}
	
	public void setIsOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public Debt removeConfirmedDebt(int i) {
		return confirmedDebts.remove(i);
	}
	
	public Debt removePendingDebt(int i) {
		return pendingDebts.remove(i);
	}
	
	@Override
	public String getClassName() {
		return "User";
	}
	
	public void addFriend(User friend) {
		if(friends == null) {
			this.friends = new ArrayList<User>();
			addVariable("friends", this.friends);
		}
		friends.add(friend);
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof User)) return false;
		return username.equals(((User)o).getUsername());
//		User u = (User) o;
//		if(this.getNumberOfVariables() != u.getNumberOfVariables()) return false;
//		for (int i = 0; i < this.getNumberOfVariables(); i++) {
//			if(!u.getVariable(this.getVariableName(i)).equals(this.getVariable(i))) return false;
//		}
//		return true;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Username: " + username + ", password: " + password);
		if(friends != null) {
			sb.append(", friends = [");
			for (int i = 0; i < friends.size(); i++) {
				sb.append(friends.get(i).toString());
				if(i < friends.size() - 1) {
					sb.append(", ");
				}
			}
			sb.append("]");
		}
		return sb.toString();
	}
}
