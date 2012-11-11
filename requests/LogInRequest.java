package requests;

import requests.xml.XMLSerializable;
import logic.User;

public class LogInRequest extends XMLSerializable {

//	private String username, password;
	
	/**
	 * Empty constructor for XML restoration
	 */
	public LogInRequest() {}
	
	public LogInRequest(String username, String password, int updatePort) {
		this(new User(username, password), false, LogInRequestStatus.UNHANDLED, updatePort);
	}
	
	public LogInRequest(User user, boolean isAccepted, LogInRequestStatus status, int updatePort) {
		setUser(user);
		setVariable("accepted", isAccepted);
		setVariable("status", status);
		setVariable("updatePort", updatePort);
	}
	
	public LogInRequest(String username, String password, boolean isAccepted, LogInRequestStatus status, int updatePort) {
		this(new User(username, password), isAccepted, status, updatePort);
	}
	
	/**
	 * We need an id for XML serialization
	 * 
	 * As only one LogInRequest will be sent between the user and the server 1
	 * should suffice as the id number.
	 */
	public long getId() {
		return 1;
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
		return getUser().getPassword();
	}
	
	public static void main(String[] args) throws Exception {
		// TODO: When somebody exists as a friend, it's friends doesn't need to be parsed! Perhaps use ID?
		CreateUserRequest cur = new CreateUserRequest("Stian", "123");
		cur.setIsAproved(true);
		String xml = cur.toXML();
		System.out.println(xml);
		cur = (CreateUserRequest) XMLSerializable.toObject(xml);
		System.out.println(cur.getUsername());
		System.out.println(cur.getPassword());
		System.out.println(cur.isApproved());
		
//		User a = new User("Stian", "123");
//		User b = new User("Arne", "qazqaz");
//		User c = new User("Jan", "JANJANHEILEDAN!");
//		a.addFriend(b);
//		a.addFriend(c);
//		String xml = a.toXML();
//		System.out.println(xml);
//		User stian = (User) XMLSerializable.toObject(xml);
//		System.out.println(stian);
		
//		List<LogInR>
//		LogInRequest a = new LogInRequest("Arne", "qazqaz");
//		LogInRequest b = new LogInRequest("Stian", "qazqaz");
//		String xml = a.toXml();
//		System.out.println(xml);
//		LogInRequest lir = (LogInRequest)XMLParsable.toObject(xml);
//		System.out.println("Username: " + lir.getUserName());
//		System.out.println("Password: " + lir.getPassword());
//		System.out.println("Accepted: " + lir.isAccepted());
	}
}
