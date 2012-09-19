package logic;

public class Debt {

	private double amount;
	private String what;
	private User from, to;
	
	public Debt(double amount, String what, User from, User to) {
		this.amount = amount;
		this.what = what;
		this.from = from;
		this.to = to;
	}
	
	public double getAmount() {
		return amount;
	}

	public String getWhat() {
		return what;
	}

	public void setWhat(String what) {
		this.what = what;
	}

	public User getFrom() {
		return from;
	}

	public void setFrom(User from) {
		this.from = from;
	}

	public User getTo() {
		return to;
	}

	public void setTo(User to) {
		this.to = to;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}
	
	
}
