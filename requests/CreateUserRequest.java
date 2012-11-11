package requests;

import requests.xml.XMLSerializable;
import logic.User;

public class CreateUserRequest extends XMLSerializable {
	
	/**
	 * Empty constructor for XML restoration
	 */
	public CreateUserRequest() {}
	
	public CreateUserRequest(String username, String password) {
		this(new User(username, password), false);
	}
	
	public CreateUserRequest(User requestedUser, boolean isApproved) {
		setVariable("requestedUser", requestedUser);
		setVariable("isApproved", isApproved);
	}
	

	/**
	 * We need an ID
	 * 
	 * @TODO We could implement a base class for requests that normally do not
	 * have an id, setting it to 1 or some other default value
	 */
	@Override
	protected long getId() {
		// TODO Auto-generated method stub
		return 1;
	}

	public void setIsAproved(boolean isApproved) {
		setVariable("isApproved", isApproved);
	}
	
	public String getUsername() {
		return getRequestedUser().getUsername();
	}
	
	public String getPassword() {
		return getRequestedUser().getPassword();
	}
	
	public boolean isApproved() {
		return (Boolean) getVariable("isApproved");
	}
	
	public User getRequestedUser() {
		return (User) getVariable("requestedUser");
	}
}
