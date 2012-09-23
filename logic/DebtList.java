package logic;
import java.util.ArrayList;
import java.util.List;


public class DebtList {

	private User user1, user2;
	private List<Debt> pendingDebts, confirmedDebts;
	
	public DebtList(User user1, User user2) {
		this.user1 = user1;
		this.user2 = user2;
		pendingDebts = new ArrayList<Debt>();
		confirmedDebts = new ArrayList<Debt>();
	}
	
	public int getNumberOfPendingDebts() {
		return pendingDebts.size();
	}
	
	public int getNumberOfConfirmedDebts() {
		return confirmedDebts.size();
	}
	
	public Debt getPendingDebt(int i) {
		return pendingDebts.get(i);
	}
	
	public Debt getConfirmedDebt(int i) {
		return confirmedDebts.get(i);
	}
	
	public void addPendingDebt(Debt d) {
		pendingDebts.add(d);
	}
	
	public void addConfirmedDebt(Debt d) {
		confirmedDebts.add(d);
	}
	
	public int getNumberOfTotalDebts() {
		return pendingDebts.size() + confirmedDebts.size();
	}
}
