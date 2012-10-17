package logic;

import requests.XMLParsable;

public class Debt extends XMLParsable {

	private long id;
	private double amount;
	private String what, comment;
	private User from, to, requestedBy;
	private boolean isDone, isConfirmed;
	
	public Debt(long id, double amount, String what, User from, User to, String comment, User requestedBy) {
		setAmount(amount);
		this.id = id;
		addVariable("id", id);
		setWhat(what);
		setFrom(from);
		setTo(to);
		setDone(false);
		setComment(comment);
		this.requestedBy = requestedBy;
		addVariable("requestedBy", requestedBy);
	}
	
	public long getId(){
		return id;
	}
	
	public boolean isConfirmed() {
		return isConfirmed;
	}
	
	public void setIsConfirmed(boolean isConfirmed) {
		this.isConfirmed = isConfirmed;
		addVariable("isConfirmed", isConfirmed);
	}
	
	public User getRequestedBy() {
		return requestedBy;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
		addVariable("comment", comment);
	}
	
	public boolean isDone() {
		return isDone;
	}
	
	public void setDone(boolean isDone) {
		this.isDone = isDone;
		addVariable("isDone", isDone);
	}
	
	public double getAmount() {
		return amount;
	}

	public String getWhat() {
		return what;
	}

	public void setWhat(String what) {
		this.what = what;
		addVariable("what", what);
	}

	public User getFrom() {
		return from;
	}

	public void setFrom(User from) {
		this.from = from;
		addVariable("from", from);
	}

	public User getTo() {
		return to;
	}

	public void setTo(User to) {
		this.to = to;
		addVariable("to", to);
	}

	public void setAmount(double amount) {
		this.amount = amount;
		addVariable("amount", amount);
	}

	@Override
	public String getClassName() {
		return "Debt";
	}
	
	public String toString() {
		return "Amount: " + amount + ", what: " + what + ", from: " + from.getUsername() + ", to: " + to.getUsername() + ", comment: " + comment;
	}
}
