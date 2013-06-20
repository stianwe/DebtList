package network;

public class Constants {

	public static final String VERSION = "0";
	
	// Network
	public static final String SERVER_ADDRESS = "invert.ed.ntnu.no";
	public static final int STANDARD_SERVER_PORT = 13337;
	public static final long STANDARD_TIME_BETWEEN_UPDATES = 5 * 60 * 1000; // 5 minutes FIXME CHANGED FOR TESTING PURPOSES!!!!! SHOULD NOT BE INCLUDED IN RELEASED TEST VERSION!!!
	public static final long TIME_BETWEEN_WRITES_TO_DATABASE = 15 * 60 * 1000; // 15 minutes
	/**
	 * For how long a session token will remain active and can receive updates
	 */
//	public static final long MINIMUM_INACTIVE_TIME_BEFORE_DISCONNECT = 10000;  // 20 minutes FIXME
	public static final long MINIMUM_INACTIVE_TIME_BEFORE_DISCONNECT = 20 * 60 * 1000; // 20 minutes
	/**
	 * How long the client will wait to receive a response from the server
	 */
	public static final int STANDARD_SOCKET_RECEIVE_TIMEOUT = 0; 
	
	public static final int STANDARD_SOCKET_CONNECT_TIMEOUT = 10 * 1000; // 10 seconds
	
	public static final String SERVER_LOG_FILE = "TEST_debtlog.txt";
	
	public static final String SESSION_TOKEN_REQUEST = "REQUEST";
	
	public static final boolean ANDROID_DEBUG_MODE = true;
	
	public static final boolean STANDARD_DISABLE_UPDATES_WHEN_NOT_ON_WIFI = false;

	public static final String SESSION_EXPIRED = "session-expired";
}
