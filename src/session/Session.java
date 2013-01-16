package session;
import gui.LogInPanel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import gui.LogInPanel;

import javax.swing.JFrame;
import javax.swing.JPanel;

import logic.Debt;
import logic.DebtStatus;
import logic.User;
import network.ClientConnection;
import requests.FriendRequest;
import requests.LogInRequest;
import requests.LogInRequestStatus;
import requests.FriendRequest.FriendRequestStatus;
import requests.xml.XMLSerializable;


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
	 * @return				The status of the received response
	 */
	public LogInRequestStatus logIn(String username, String password) {
		LogInRequest resp = null;
		try {
			send(new LogInRequest(username, password).toXML());
			resp = (LogInRequest) XMLSerializable.toObject(receive());
			if(resp.isAccepted()) {
				setUser(resp.getUser());
			}
		} catch(IOException e) {
			// TODO 
			e.printStackTrace();
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
	 * @throws IOException if an error occurs while trying to receive messages
	 * @return The received message
	 */
	public String receive() throws IOException {
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
			if(d.getStatus() == DebtStatus.MERGE) {
				System.out.println("Received a merged debt!");
				// Remove all debts confirmed between these two users with the same currency, that are not completed by any users
				List<Debt> debtsToRemove = new ArrayList<Debt>();
				for (int i = 0; i < getUser().getNumberOfConfirmedDebts(); i++) {
					Debt c = getUser().getConfirmedDebt(i); 
					if(c.getStatus() == DebtStatus.CONFIRMED) {
						if((c.getFrom().equals(d.getFrom()) && c.getTo().equals(d.getTo())) || (c.getFrom().equals(d.getTo()) && c.getTo().equals(d.getFrom()))) {
							debtsToRemove.add(c);
						}
					}
				}
				for (Debt i : debtsToRemove) {
					getUser().removeConfirmedDebt(i);
				}
				// Also remove the pending debt that was accepted
				getUser().removePendingDebt(d);
				// Add the new merged debt if it's amount is not zero
				if(Math.abs(d.getAmount()) != 0) {
					d.setStatus(DebtStatus.CONFIRMED);
				} else {
					d.setStatus(DebtStatus.COMPLETED);
				}
				getUser().addConfirmedDebt(d);
				return;
			}
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
		} else if(o instanceof FriendRequest) {
			FriendRequest req = (FriendRequest) o;
			switch(req.getStatus()) {
			case DECLINED:
				// TODO: Notify user (or?)
				break;
			case ACCEPTED:
				// Someone accepted our friend request, add him/her as friend
				getUser().addFriend(new User(req.getFriendUsername()));
				break;
			case PENDING:
				// We received a new friend request, add it
				getUser().addFriendRequest(req);
				break;
			}
		} else {
			System.out.println("ERROR: Received something unknown!");
		}
	}
}
