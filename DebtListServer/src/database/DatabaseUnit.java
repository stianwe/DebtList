package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import config.Config;
import config.ConfigManager;

import requests.FriendRequest;
import requests.FriendRequest.FriendRequestStatus;

import logic.Debt;
import logic.DebtStatus;
import logic.User;

public class DatabaseUnit {

	public static final String CONFIG_FILE = "DebtList_server.conf";
	
	// Database tables
	public static final String TABLE_USER = "user";
	public static final String TABLE_DEBT = "debt";
	public static final String TABLE_FRIEND_REQUEST = "friendRequest";
	
	// Database fields
	public static final String FIELD_USER_USERNAME = "username";
	public static final String FIELD_USER_PASSWORD = "password";
	public static final String FIELD_USER_ID = "id";
	public static final String FIELD_USER_EMAIL = "email";
	public static final String FIELD_USER_ACTIVATION_KEY = "activationKey";
	public static final String FIELD_USER_IS_ACTIVATED = "isActivated";
	
	public static final String FIELD_DEBT_ID = "id";
	public static final String FIELD_DEBT_AMOUNT = "amount";
	public static final String FIELD_DEBT_WHAT = "what";
	public static final String FIELD_DEBT_TO_USER = "toUser";
	public static final String FIELD_DEBT_FROM_USER = "fromUser";
	public static final String FIELD_DEBT_COMMENT = "comment";
	public static final String FIELD_DEBT_STATUS = "status";
	public static final String FIELD_DEBT_REQUESTED_BY_USER = "requestedByUser";
	
	public static final String FIELD_FRIEND_REQUEST_ID = "id";
	public static final String FIELD_FRIEND_REQUEST_TO_USER = "toUser";
	public static final String FIELD_FRIEND_REQUEST_FROM_USER = "fromUser";
	public static final String FIELD_FRIEND_REQUEST_STATUS = "status";
	
	private Statement st;
	private Connection con;
	
	private String dbUsername;
	private String dbPassword;
	private String dbHostName;
	private String dbName;
	private int dbPort;
	
	public DatabaseUnit() {
		// Load config file
		Config config = ConfigManager.loadConfig(CONFIG_FILE);
		dbUsername = config.getMySQLUsername();
		dbPassword = config.getMySQLPassword();
		dbHostName = config.getMySQLHostName();
		dbName = config.getMySQLDBName();
		dbPort = config.getMySQLPort();
	}
	
	/**
	 * Connects to the database specified with this class' constants
	 * @throws Exception 
	 */
	public void connect() throws Exception {
		//Register the JDBC driver for MySQL.
		Class.forName("com.mysql.jdbc.Driver");

		//Define URL of database server for
		String url =
//				"jdbc:" + DB_HOST_NAME + ":" + DB_PORT + "/" + DB_NAME;
				"jdbc:" + dbHostName + ":" + dbPort + "/" + dbName;

		//Get a connection to the database for a
		con = DriverManager.getConnection(url, dbUsername, dbPassword);

		//Display URL and connection information
		System.out.println("URL: " + url);
		System.out.println("Connection: " + con);

		//Get a Statement object
		st = con.createStatement();
	}
	
	/**
	 * Attempts to close the connection to the database
	 */
	public void close() {
		try {
			con.close();
		} catch (SQLException e) {}
	}
	
	/**
	 * Returns the next available id from the specified table
	 * @param tableName	The table to fetch the id from (use constants)
	 * @param fieldName	The id field (probably just "id", but use constants)
	 * @return			The next available id
	 * @throws SQLException
	 */
	public long getNextId(String tableName, String fieldName) throws SQLException {
		ResultSet rs = st.executeQuery("SELECT MAX(" + fieldName + ") FROM " + tableName);
		if(!rs.next())
			return 1;
		return rs.getLong(1) + 1;
	}
	
	/**
	 * Returns the users contained in the database
	 * @return	A map with the users as keys and their passwords as values
	 * @throws SQLException
	 */
	public Map<User, String> loadUsers() throws SQLException {
		ResultSet rs = st.executeQuery("SELECT * FROM " + TABLE_USER);
		Map<User, String> users = new HashMap<User, String>();
		while(rs.next()) {
			users.put(new User(rs.getLong(FIELD_USER_ID), rs.getString(FIELD_USER_USERNAME), rs.getString(FIELD_USER_EMAIL), rs.getString(FIELD_USER_ACTIVATION_KEY), (rs.getString(FIELD_USER_IS_ACTIVATED).equals("0") ? false : true)), rs.getString(FIELD_USER_PASSWORD));
		}
		return users;
	}
	
	/**
	 * Will load the users' friends and their friend request
	 * @param users			The users to load friends and friend requests for, represented
	 * 						in a map with the user name as key, and user object as value
	 * @throws SQLException
	 */
	public void loadFriends(Map<String, User> users) throws SQLException {
		ResultSet rs = st.executeQuery("SELECT * FROM " + TABLE_FRIEND_REQUEST);
		while(rs.next()) {
			FriendRequestStatus status = Enum.valueOf(FriendRequestStatus.class, rs.getString(FIELD_FRIEND_REQUEST_STATUS));
			switch(status) {
			case ACCEPTED:
				// Add the users as friends
				users.get(rs.getString(FIELD_FRIEND_REQUEST_FROM_USER)).addFriend(users.get(rs.getString(FIELD_FRIEND_REQUEST_TO_USER)));
				users.get(rs.getString(FIELD_FRIEND_REQUEST_TO_USER)).addFriend(users.get(rs.getString(FIELD_FRIEND_REQUEST_FROM_USER)));
				// Fall through to add the friend request
			case PENDING:
				FriendRequest r = new FriendRequest(rs.getString(FIELD_FRIEND_REQUEST_TO_USER), users.get(rs.getString(FIELD_FRIEND_REQUEST_FROM_USER)), status, rs.getLong(FIELD_FRIEND_REQUEST_ID));
				// Add the friend request to the target friend
				users.get(rs.getString(FIELD_FRIEND_REQUEST_TO_USER)).addFriendRequest(r);
				// Also add the request to the requesting user
				users.get(rs.getString(FIELD_FRIEND_REQUEST_FROM_USER)).addFriendRequest(r);
				break;
			default:
				System.out.println("Skipped FriendRequest with weird status while loading friends.");
			}
		}
	}

	/**
	 * Converts the given list of users to a map with the users' user name as key and the user objects as values,
	 * as used by this class' methods
	 * @param users	The users in a list
	 * @return		The users in a map, indexed with the users' user name
	 */
	public static Map<String, User> listToMap(Collection<User> users) {
		Map<String, User> map = new HashMap<String, User>();
		for (User u : users) {
			map.put(u.getUsername(), u);
		}
		return map;
	}
	
	/**
	 * Load the given users' debts. REQUESTED and DECLINED debts will be put in pending debts.
	 * CONFIRMED, COMPLETED_BY_TO, COMPLETED_BY_FROM, MERGE and COMPLETED will be put in confirmed debts.
	 * @param users	The users
	 * @throws SQLException
	 */
	public void loadDebts(Map<String, User> users) throws SQLException {
		ResultSet rs = st.executeQuery("SELECT * FROM " + TABLE_DEBT);
		while(rs.next()) {
			Debt d = new Debt(rs.getLong(FIELD_DEBT_ID), rs.getDouble(FIELD_DEBT_AMOUNT), rs.getString(FIELD_DEBT_WHAT), users.get(rs.getString(FIELD_DEBT_FROM_USER)), users.get(rs.getString(FIELD_DEBT_TO_USER)), rs.getString(FIELD_DEBT_COMMENT), users.get(rs.getString(FIELD_DEBT_REQUESTED_BY_USER)), Enum.valueOf(DebtStatus.class, rs.getString(FIELD_DEBT_STATUS)));
			switch(d.getStatus()) {
			case REQUESTED:
			case DECLINED:
				d.getFrom().addPendingDebt(d);
				d.getTo().addPendingDebt(d);
				break;
			case CONFIRMED:
			case COMPLETED_BY_TO:
			case COMPLETED_BY_FROM:
			case MERGE:
			case COMPLETED:
				d.getFrom().addConfirmedDebt(d);
				d.getTo().addConfirmedDebt(d);
				break;
			case DELETED:
				// Don't load this debt
				break;
			default:
				System.out.println("Failed loading debt: " + d);
			}
		}
	}
	
	/**
	 * Saves ALL the given users (make sure you only send users that has been updated, or that are to be created)
	 * Note: Friends will only be saved if at least the user with the friend request is passed as argument TODO: Is this stupid?
	 * @param users			A list containing all the users to save
	 * @param passwords		A map on the following form: <user name, password>
	 * @throws SQLException
	 */
	public void save(Collection<User> users, Map<String, String> passwords) throws SQLException {
		System.out.println("Saving..");
		// Maintain a map between user ids and user names to easily look them up when saving debts and friend requests
		Map<String, Long> userIds = new HashMap<String, Long>();
		// Save all users first, because the users must already exist before inserting any friend requests or debts they are referenced in
		for (User u : users) {
			System.out.println("Writing " + u.getUsername() + " to database..");
			// Check if this is a new user
			if(SQLHelper.exists(con, TABLE_USER, FIELD_USER_ID, u.getId() + "")) {
				// User already exists
				System.out.println("User already exists.");
				SQLHelper.update(con, TABLE_USER, new String[]{FIELD_USER_EMAIL, FIELD_USER_PASSWORD, FIELD_USER_IS_ACTIVATED, FIELD_USER_ACTIVATION_KEY}, new String[]{u.getEmail(), passwords.get(u.getUsername()), (u.isActivated() ? "1" : "0"), u.getActivationKey()}, FIELD_USER_USERNAME, '"' + u.getUsername() + '"');
			} else {
				// New user
				System.out.println("Inserting user into database.");
				SQLHelper.insert(con, TABLE_USER, new String[]{FIELD_USER_USERNAME, FIELD_USER_PASSWORD, FIELD_USER_EMAIL, FIELD_USER_ACTIVATION_KEY, FIELD_USER_IS_ACTIVATED}, new String[]{u.getUsername(), passwords.get(u.getUsername()), u.getEmail(), u.getActivationKey(), (u.isActivated() ? "1" : "0")});
			}
			userIds.put(u.getUsername(), u.getId());
		}
		for (User u : users) {
			// Save the friends (from the requests, since all friends must have sent a request some time)
			System.out.println("Number of friend requests: " + u.getNumberOfFriendRequests());
			for (int i = 0; i < u.getNumberOfFriendRequests(); i++) {
				FriendRequest req = u.getFriendRequest(i);
				// Check if the request already exists
				if(SQLHelper.exists(con, TABLE_FRIEND_REQUEST, FIELD_FRIEND_REQUEST_ID, req.getId() + "")) {
					// Update the value that CAN change (no matter if it actually has changed)
					SQLHelper.update(con, TABLE_FRIEND_REQUEST, new String[]{FIELD_FRIEND_REQUEST_STATUS}, new String[]{req.getStatus().toString()}, FIELD_FRIEND_REQUEST_ID, req.getId() + "");
				} else {
					// If not, create it
					SQLHelper.insert(con, TABLE_FRIEND_REQUEST, 
							new String[]{FIELD_FRIEND_REQUEST_ID, FIELD_FRIEND_REQUEST_TO_USER, FIELD_FRIEND_REQUEST_FROM_USER, FIELD_FRIEND_REQUEST_STATUS}, 
							new String[]{req.getId() + "", userIds.get(req.getFriendUsername()) + "", userIds.get(req.getFromUser().getUsername()) + "", req.getStatus().toString()});
				}
			}
			// Save the debts
			// Pending
			for (int i = 0; i < u.getNumberOfPendingDebts(); i++) {
				SQLHelper.updateDebt(con, u.getPendingDebt(i), userIds);
			}
			// Confirmed
			for (int i = 0; i < u.getNumberOfConfirmedDebts(); i++) {
				SQLHelper.updateDebt(con, u.getConfirmedDebt(i), userIds);
			}
		}
		System.out.println("Done saving.");
	}
}
