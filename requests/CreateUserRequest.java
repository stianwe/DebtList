package requests;

import logic.User;

public class CreateUserRequest extends XMLParsable {

	private User requestedUser;
	private boolean isApproved;
	
	public CreateUserRequest(String username, String password) {
		this.requestedUser = new User(username, password);
		this.addVariable("requestedUser", this.requestedUser);
		this.isApproved = false;
		this.addVariable("isApproved", this.isApproved);
	}
	
	public CreateUserRequest(User requestedUser, boolean isApproved) {
		this.requestedUser = requestedUser;
		this.addVariable("requestedUser", this.requestedUser);
		this.isApproved = isApproved;
		this.addVariable("isApproved", this.isApproved);
	}
	
	@Override
	public String getClassName() {
		return "CreateUserRequest";
	}

	public void setIsAproved(boolean isApproved) {
		this.isApproved = isApproved;
		this.updateVariable("isApproved", this.isApproved);
	}
	
	public String getUsername() {
		return requestedUser.getUsername();
	}
	
	public String getPassword() {
		return requestedUser.getPassword();
	}
	
	public boolean isApproved() {
		return isApproved;
	}
	
	public User getRequestedUser() {
		return requestedUser;
	}
}
