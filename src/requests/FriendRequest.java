package requests;

import logic.User;

public class FriendRequest extends Request {

	public enum FriendRequestStatus {
		/**
		 * The server has not yet handled the request
		 */
		UNHANDLED, 
		/**
		 * The target user has accepted the request
		 */
		ACCEPTED, 
		/**
		 * The target user has declined the request
		 */
		DECLINED,
		/**
		 * The server has registered the request, but the target user has not yet responded
		 */
		PENDING, 
		/**
		 * The target user was not found
		 */
		USER_NOT_FOUND,
		/**
		 * This request has already been sent
		 */
		ALREADY_EXISTS;
	}
	
	/**
	 * Default constructor.
	 * Should not be used by anyone other than the XMLSerializer.
	 */
	public FriendRequest() {}
	
	/**
	 * Creates a new FriendRequest with the given values, -1 as id, and UNHANDLED as status.
	 * @param friendUsername	The target friend's username
	 * @param from				The requesting user
	 */
	public FriendRequest(String friendUsername, User from) {
		this(friendUsername, from, FriendRequestStatus.UNHANDLED, -1);
	}
	
	/**
	 * 
	 * @param friendUsername
	 * @param from
	 * @param status
	 * @param id
	 */
	public FriendRequest(String friendUsername, User from, FriendRequestStatus status, long id) {
		setFriendUsername(friendUsername);
		setFromUser(from);
		setStatus(status);
		setId(id);
	}

	public void setStatus(FriendRequestStatus status) {
		setVariable("status", status);
	}
	
	public void setFriendUsername(String username) {
		setVariable("friendUsername", username);
	}
	
	public void setFromUser(User user) {
		setVariable("fromUser", user);
	}
	
	public User getFromUser() {
		return (User) getVariable("fromUser");
	}
	
	public String getFriendUsername() {
		return (String) getVariable("friendUsername");
	}
	
	public FriendRequestStatus getStatus() {
		return (FriendRequestStatus) getVariable("status");
	}
	
	public void setId(long id) {
		setVariable("id", id);
	}
	
	@Override
	public long getId() {
		return (long) getVariable("id");
	}
}
