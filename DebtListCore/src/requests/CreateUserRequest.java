package requests;

import network.Constants;
import logic.User;

public class CreateUserRequest extends Request {
	
	/**
	 * Empty constructor for XML restoration
	 */
	public CreateUserRequest() {}
	
	/**
	 * Initializes a CreateUserRequest with the given arguments, and the status set to UNHANDLED as default
	 * @param username
	 * @param password
	 * @param email
	 */
	public CreateUserRequest(String username, String password, String email) {
		this(new User(username), password, CreateUserRequestStatus.UNHANDLED);
		getRequestedUser().setEmail(email);
	}
	
	/**
	 * @deprecated isApproved should no longer be used
	 * @param requestedUser
	 * @param password
	 * @param isApproved
	 */
	public CreateUserRequest(User requestedUser, String password, boolean isApproved) {
		setVariable("requestedUser", requestedUser);
		setIsAproved(isApproved);
		setVariable("password", password);
		// Set the version to the one in the constants
		System.out.println("Setting version to " + Constants.VERSION);
		setVersion(Constants.VERSION);
	}

	/**
	 * 
	 * @param requestedUser
	 * @param password
	 * @param status
	 */
	public CreateUserRequest(User requestedUser, String password, CreateUserRequestStatus status) {
		setVariable("requestedUser", requestedUser);
		setVariable("password", password);
		setStatus(status);
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
	
	/**
	 * @deprecated Deprecated after status was added. Use setStatus() instead
	 * @param isApproved
	 */
	public void setIsAproved(boolean isApproved) {
		setVariable("isApproved", isApproved);
	}
	
	public void setStatus(CreateUserRequestStatus status) {
		setVariable("status", status);
	}
	
	public CreateUserRequestStatus getStatus() {
		return (CreateUserRequestStatus) getVariable("status");
	}
	
	public String getUsername() {
		return getRequestedUser().getUsername();
	}
	
	public String getPassword() {
		return (String) getVariable("password");
	}
	
//	/**
//	 * @deprecated Deprecated after status was added. Use getStatus() instead
//	 * @return
//	 */
//	public boolean isApproved() {
//		return (Boolean) getVariable("isApproved");
//	}
	
	public boolean isApproved() {
		return getStatus() == CreateUserRequestStatus.ACCEPTED;
	}
	
	public User getRequestedUser() {
		return (User) getVariable("requestedUser");
	}
}
