package requests;

public enum LogInRequestStatus {
	/**
	 * The request has not been processed by the server
	 */
	UNHANDLED, 
	/**
	 * The request was accepted by the server, and the user is now logged in
	 */
	ACCEPTED, 
	/**
	 * Wrong information (user name/email or password) was submitted
	 */
	WRONG_INFORMATION, 
	/**
	 * The user is already logged on
	 * @deprecated Users should now be able to log on from several devices at once, so this should not be used anymore
	 */
	ALREADY_LOGGED_ON, 
	/**
	 * The user cannot log in, since it is not yet activated
	 */
	NOT_ACTIVATED, 
	/**
	 * The attached activation key did not match the one registered by the server
	 */
	INVALID_ACTIVATION_KEY,
	/**
	 * The client that tried to log in is using an incompatible version 
	 */
	INCOMPATIBLE_CLIENT_VERSION,
	;
}