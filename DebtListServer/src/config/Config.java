package config;

import requests.xml.XMLSerializable;

public class Config extends XMLSerializable{

	@Override
	protected long getId() {
		return 0;
	}

	/*
	 * SQL configs
	 */
	
	public void setMySQLUsername(String username) {
		setVariable("MySQLUsername", username);
	}
	
	public void setMySQLPassword(String password) {
		setVariable("MySQLPassword", password);
	}
	
	public String getMySQLUsername() {
		return (String) getVariable("MySQLUsername");
	}
	
	public String getMySQLPassword() {
		return (String) getVariable("MySQLPassword");
	}
	
	public void setMySQLHostName(String hostName) {
		setVariable("MySQLHostName", hostName);
	}
	
	public String getMySQLHostName() {
		return (String) getVariable("MySQLHostName");
	}
	
	public void setMySQLDBName(String dbName) {
		setVariable("MySQLDBName", dbName);
	}
	
	public String getMySQLDBName() {
		return (String) getVariable("MySQLDBName");
	}
	
	public void setMySQLPort(int port) {
		setVariable("MySQLPort", port);
	}
	
	public int getMySQLPort() {
		return (Integer) getVariable("MySQLPort");
	}
	
	/*
	 * Mail server configs
	 */
	
	public void setSupportEmail(String mail) {
		setVariable("supportEmail", mail);
	}
	
	public String getSupportEmail() {
		return (String) getVariable("supportEmail");
	}
	
	public void setSupportEmailUsername(String username) {
		setVariable("supportEmailUsername", username);
	}
	
	public String getSupportEmailUsername() {
		return (String) getVariable("supportEmailUsername");
	}
	
	public void setSupportEmailPassword(String password) {
		setVariable("supportEmailPassword", password);
	}
	
	public String getSupportEmailPassword() {
		return (String) getVariable("supportEmailPassword");
	}
}
