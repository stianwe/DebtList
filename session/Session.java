package session;
import gui.LogInPanel;

import javax.swing.JFrame;
import javax.swing.JPanel;

import requests.LogInRequest;
import requests.LogInRequestStatus;
import requests.XMLParsable;

import logic.Debt;
import logic.DebtStatus;
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
	}
	
	/**
	 * This method starts the GUI by displaying the log in screen
	 */
	public void startGUI() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		currentPanel = new LogInPanel(); 
		frame.add(currentPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setSize(frame.getWidth() + 100, frame.getHeight() + 100);
	}
	
	/**
	 * Checks if this Session's user's connection is connected to the server
	 * @return	True if connected, false if not
	 */
	public boolean isConnected() {
		return connection.isConnected();
	}
	
	/**
	 * Checks if this Session's user is logged in
	 * @return	True if the user is logged in, false if not
	 */
	public boolean isLoggedIn() {
		return user != null;
	}
	
	/**
	 * Tries to log in by sending a LogInRequest to the server connected to by the connection
	 * @param username		The user name
	 * @param password		The password
	 * @param updatePort	The port to send updates
	 * @return				The status of the received response
	 */
	public LogInRequestStatus logIn(String username, String password, int updatePort) {
		send(new LogInRequest(username, password, updatePort).toXml());
		LogInRequest resp = (LogInRequest) XMLParsable.toObject(receive());
		if(resp.isAccepted()) {
			setUser(resp.getUser());
		}
		return resp.getStatus();
	}
	
	/**
	 * Connects t the given host at the given port, if not already connected to a host
	 * @param host
	 * @param port
	 */
	public void connect(String host, int port) {
		if(!isConnected()) {
			connection.connect(host, port);
		}
	}
	
	/**
	 * Sends the given message to the connected host
	 * @param msg	The message to send
	 */
	public void send(String msg) {
		connection.send(msg);
	}

	/**
	 * Tries to receive a message from the connected host
	 * @return	The received message
	 */
	public String receive() {
		return connection.receive();
	}
	
	/**
	 * Adds the given JPanel to this Session's JFrame
	 * @param panel	The JPanel to add
	 */
	public void addPanel(JPanel panel) {
		currentPanel.setVisible(false);
		currentPanel = panel;
		currentPanel.setVisible(true);
		frame.add(panel);
		fixFrame();
	}
	
	/**
	 * Fixes this Session's JFrame by repainting it, packing it, moving it to the middle and setting it's size by adding some space
	 */
	public void fixFrame() {
		frame.repaint();
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setSize(frame.getWidth() + 100, frame.getHeight() + 100);
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	public void setFrame(JFrame frame) {
		this.frame = frame;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
	
	public void processUpdate(Object o) {
		System.out.println("Update received: " + o);
		if(o instanceof Debt) {
			Debt d = (Debt) o;
			// TODO: Would this take care of accepting/declining?
			// TODO: Will the users be correct?
			// TODO: Process new Debt and updated Debt!
			// Check if Debt already exists
			//for...
			for (int i = 0; i < (user.getNumberOfConfirmedDebts() > user.getNumberOfPendingDebts() ? user.getNumberOfConfirmedDebts(): user.getNumberOfPendingDebts()); i++) {
				if(user.getNumberOfConfirmedDebts() > i && user.getConfirmedDebt(i).getId() == d.getId()) {
					user.removeConfirmedDebt(i);
					user.addConfirmedDebt(d);
					return;
				}
				if(user.getNumberOfPendingDebts() > i && user.getPendingDebt(i).getId() == d.getId()) {
					user.removePendingDebt(i);
					if(d.isConfirmed()) user.addConfirmedDebt(d);
					else if(d.getStatus() != DebtStatus.DECLINED) user.addPendingDebt(d);
					return;
				}
			}
			// Should probably add the debt in one of the lists if the method reaches this far?
			if(d.isConfirmed()) user.addConfirmedDebt(d);
			else user.addPendingDebt(d);
		}
	}
}
