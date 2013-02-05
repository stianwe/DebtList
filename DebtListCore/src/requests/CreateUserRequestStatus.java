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
	ACCEPTED;
}
