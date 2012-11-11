package requests;

import logic.User;

public class LogInRequest extends Request {

	/**
	 * Empty constructor for XML restoration
	 */
	public LogInRequest() {}
	
	public LogInRequest(String username, String password, int updatePort) {
		this(new User(username), password, false, LogInRequestStatus.UNHANDLED, updatePort);
	}
	
	public LogInRequest(User user, String password, boolean isAccepted, LogInRequestStatus status, int updatePort) {
		setUser(user);
		setVariable("accepted", isAccepted);
		setVariable("status", status);
		setVariable("updatePort", updatePort);
		setVariable("password", password);
	}
	
	public LogInRequest(String username, String password, boolean isAccepted, LogInRequestStatus status, int updatePort) {
		this(new User(username), password, isAccepted, status, updatePort);
	}
	
	public void setStatus(LogInRequestStatus status) {
		setVariable("status", status);
	}
	
	public void setUser(User u) {
		setVariable("user", u);
	}
	
	public boolean isAccepted() {
		return (Boolean) getVariable("accepted");
	}
	
	public int getUpdatePort() {
		return (Integer) getVariable("updatePort");
	}
	
	public User getUser() {
		return (User) getVariable("user");
	}
	
	public LogInRequestStatus getStatus() {
		return (LogInRequestStatus) getVariable("status");
	}
	
	public void setAccepted(boolean isAccepted) {
		setVariable("accepted", isAccepted);
	}
		
	public String getUserName() {
		return getUser().getUsername();
	}
	
	public String getPassword() {
		return (String) getVariable("password");
	}
}
