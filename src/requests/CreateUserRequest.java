package requests;

import logic.User;

public class CreateUserRequest extends Request {
	
	/**
	 * Empty constructor for XML restoration
	 */
	public CreateUserRequest() {}
	
	public CreateUserRequest(String username, String password) {
		this(new User(username), password, false);
	}
	
	public CreateUserRequest(User requestedUser, String password, boolean isApproved) {
		setVariable("requestedUser", requestedUser);
		setVariable("isApproved", isApproved);
		setVariable("password", password);
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
