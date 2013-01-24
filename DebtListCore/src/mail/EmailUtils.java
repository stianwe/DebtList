package mail;

public class EmailUtils {

	/**
	 * Checks if the given email is valid, i.e. have a '@' followed by a '.'
	 * @param email	The email to check	
	 * @return		True if the email is valid, false if not
	 */
	public static boolean verifyEmail(String email) {
		// Simple email verification check
		int ma = email.indexOf('@');
		int md = email.lastIndexOf('.');
		return !(ma == -1 || md == -1 || md < ma); 
	}
}
