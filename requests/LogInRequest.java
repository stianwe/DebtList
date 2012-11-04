package requests;

import logic.User;

public class LogInRequest extends XMLParsable {

//	private String username, password;
	private User uUser;
	private boolean accepted;
	private LogInRequestStatus status;
	private int updatePort;
	
	public LogInRequest(String username, String password, int updatePort) {
		this(new User(username, password), false, LogInRequestStatus.UNHANDLED, updatePort);
	}
	
	public LogInRequest(User user, boolean isAccepted, LogInRequestStatus status, int updatePort) {
		this.accepted = isAccepted;
		this.status = status;
		this.updatePort = updatePort;
		setUser(user);
		addVariable("accepted", this.accepted);
		addVariable("status", this.status);
		addVariable("updatePort", updatePort);
	}
	
	public LogInRequest(String username, String password, boolean isAccepted, LogInRequestStatus status, int updatePort) {
		this(new User(username, password), isAccepted, status, updatePort);
	}
	
	public void setStatus(LogInRequestStatus status) {
		this.status = status;
		updateVariable("status", status);
	}
	
	public void setUser(User u) {
		this.uUser = u;
		addVariable("uUser", uUser);
	}
	
	public boolean isAccepted() {
		return accepted;
	}
	
	public int getUpdatePort() {
		return updatePort;
	}
	
	public User getUser() {
		return uUser;
	}
	
	public LogInRequestStatus getStatus() {
		return status;
	}
	
	public void setAccepted(boolean isAccepted) {
		this.accepted = isAccepted;
		updateVariable("accepted", isAccepted);
//		updateVariable("accepted", isAccepted ? "true" : "false");
	}
	
	@Override
	public String getClassName() {
		return "LogInRequest";
	}
	
	public String getUserName() {
//		return username;
		return uUser.getUsername();
	}
	
	public String getPassword() {
//		return password;
		return uUser.getPassword();
	}
	
	public static void main(String[] args) {
		// TODO: When somebody exists as a friend, it's friends doesn't need to be parsed! Perhaps use ID?
		CreateUserRequest cur = new CreateUserRequest("Stian", "123");
		cur.setIsAproved(true);
		String xml = cur.toXml();
		System.out.println(xml);
		cur = (CreateUserRequest) XMLParsable.toObject(xml);
		System.out.println(cur.getUsername());
		System.out.println(cur.getPassword());
		System.out.println(cur.isApproved());
		
//		User a = new User("Stian", "123");
//		User b = new User("Arne", "qazqaz");
//		User c = new User("Jan", "JANJANHEILEDAN!");
//		a.addFriend(b);
//		a.addFriend(c);
//		String xml = a.toXml();
//		System.out.println(xml);
//		User stian = (User) XMLParsable.toObject(xml);
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
