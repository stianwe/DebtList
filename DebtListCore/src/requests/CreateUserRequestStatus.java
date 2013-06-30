package requests;

public enum CreateUserRequestStatus {

	/**
	 * The request has not been processed by the server
	 */
	UNHANDLED, 
	/**
	 * The requested user name is already taken
	 */
	USERNAME_ALREADY_TAKEN,
	/**
	 * The requested user name is invalid. I.e. over 30 characters
	 */
	INVALID_USERNAME,
	/**
	 * The email is already registered
	 */
	EMAIL_ALREADY_REGISTERED, 
	/**
	 * The request was accepted by the server, and the user has been
	 * created (but not activated)
	 */
	ACCEPTED,
	/**
	 * The client that tried to register a user is using an incompatible version 
	 */
	INCOMPATIBLE_CLIENT_VERSION,
	/**
	 * The server was not able to send the welcome message
	 */
	COULD_NOT_SEND_WELCOME_MESSAGE,
	;
}
