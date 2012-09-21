package requests;

import logic.User;

public class LogInRequest extends XMLParsable {

	private String username, password;
	private boolean accepted;
	private LogInRequestStatus status;
	
	public LogInRequest(String username, String password) {
		this.username = username;
		this.password = password;
		this.accepted = false;
		this.status = LogInRequestStatus.UNHANDLED;
		addVariable("username", this.username);
		addVariable("password", this.password);
		addVariable("accepted", this.accepted);
		addVariable("status", this.status);
	}
	
	public LogInRequest(String username, String password, boolean isAccepted, LogInRequestStatus status) {
		this(username, password);
		this.accepted = isAccepted;
		this.status = status;
	}
	
	public void setStatus(LogInRequestStatus status) {
		this.status = status;
		updateVariable("status", status);
	}
	
	public boolean isAccepted() {
		return accepted;
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
		return username;
	}
	
	public String getPassword() {
		return password;
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
