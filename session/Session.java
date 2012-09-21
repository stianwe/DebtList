package session;
import gui.LogInPanel;

import javax.swing.JFrame;
import javax.swing.JPanel;

import logic.User;

import network.ClientConnection;


public class Session {

	public static Session session = new Session();
	private JFrame frame;
	private JPanel currentPanel;
	private User user;
	
	private ClientConnection connection;
	
	public Session() {
		connection = new ClientConnection();
		
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		currentPanel = new LogInPanel(); 
		frame.add(currentPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setSize(frame.getWidth() + 100, frame.getHeight() + 100);
	}
	
	public boolean isConnected() {
		return connection.isConnected();
	}
	
	public void connect(String host, int port) {
		if(!isConnected()) {
			connection.connect(host, port);
		}
	}
	
	public void send(String msg) {
		connection.send(msg);
	}
	
	public String receive() {
		return connection.receive();
	}
	
	public void addPanel(JPanel panel) {
		currentPanel.setVisible(false);
		currentPanel = panel;
		currentPanel.setVisible(true);
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setSize(frame.getWidth() + 100, frame.getHeight() + 100);
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
	
	public static void main(String[] args) {
		System.out.println("Starting..");
	}
}
