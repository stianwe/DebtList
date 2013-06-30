package mail;

public abstract class ActivationMailText {

	public static String generateSubject(String username) {
		return "Welcome to DebtList, " + username + "!";
	}
	
	public static String generateMessage(String username, String activationKey, String supportEmail) {
		// TODO: Also supply link for account activation
		return "Welcome to DebtList, " + username + "!\n\n" +
				"This is your activation key, which you need to log in to DebtList: " + activationKey + ".\n\n" +
				"If you have any problems with registration, please do not hesitate to contact us at " + supportEmail + ".\n\n" + 
				"Best regards,\n" + 
				"The DebtList team";
	}
}
