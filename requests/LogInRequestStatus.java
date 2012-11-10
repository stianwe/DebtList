package requests;

public enum LogInRequestStatus {
	UNHANDLED, ACCEPTED, WRONG_INFORMATION, ALREADY_LOGGED_ON;
	
	public static int NUMBER_OF_STATUSES = 4;
	
//	public String toString() {
//		for (int i = 0; i < NUMBER_OF_STATUSES; i++) {
//			if(this.ordinal() == i) {
//				return i + "";
//			}
//		}
//		return -1 + "";
//	}
}