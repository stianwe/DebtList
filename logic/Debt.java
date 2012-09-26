package logic;

public class Debt {

	private double amount;
	private String what, comment;
	private User from, to, requestedBy;
	private boolean isDone;
	
	public Debt(double amount, String what, User from, User to, String comment, User requestedBy) {
		this.amount = amount;
		this.what = what;
		this.from = from;
		this.to = to;
		this.isDone = false;
		this.comment = comment;
		this.requestedBy = requestedBy;
	}
	
	public User getRequestedBy() {
		return requestedBy;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public boolean isDone() {
		return isDone;
	}
	
	public void setDone(boolean isDone) {
		this.isDone = isDone;
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
