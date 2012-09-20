package logic;
import java.util.List;


public class DebtList {

	private User user1, user2;
	private List<Debt> pendingDebts, confirmedDebts;
	
	public DebtList(User user1, User user2) {
		this.user1 = user1;
		this.user2 = user2;
	}
}
