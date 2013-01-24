package requests;

import network.Constants;
import logic.User;

public class CreateUserRequest extends Request {
	
	/**
	 * Empty constructor for XML restoration
	 */
	public CreateUserRequest() {}
	
	public CreateUserRequest(String username, String password, String email) {
		this(new User(username), password, false);
		getRequestedUser().setEmail(email);
	}
	
	public CreateUserRequest(User requestedUser, String password, boolean isApproved) {
		setVariable("requestedUser", requestedUser);
		setVariable("isApproved", isApproved);
		setVariable("password", password);
		// Set the version to the one in the constants
		System.out.println("Setting version to " + Constants.VERSION);
		setVersion(Constants.VERSION);
	}

	/**
	 * Sets the version number attached to the create user request
	 * Does not normally need to be set, as it is set to DebtListCore.network.Constants by the constructors
	 * (except the default one, which is (should be!) only used by the XMLSerializer).
	 * @param version	The version, typically found in DebtListCore.network.Constants
	 */
	public void setVersion(String version) {
		setVariable("version", version);
	}
	
	/**
	 * @return The version of the DebtList this request originated from.
	 * Typically found in DebtListCore.network.Constants.VERSION.
	 */
	public String getVersion() {
		return (String) getVariable("version");
	}
	
	public void setIsAproved(boolean isApproved) {
		setVariable("isApproved", isApproved);
	}
	
	public String getUsername() {
		return getRequestedUser().getUsername();
	}
	
	public String getPassword() {
		return (String) getVariable("password");
	}
	
	public boolean isApproved() {
		return (Boolean) getVariable("isApproved");
	}
	
	public User getRequestedUser() {
		return (User) getVariable("requestedUser");
	}
}
