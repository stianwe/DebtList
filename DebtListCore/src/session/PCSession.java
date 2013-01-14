package session;
import java.io.IOException;

//import gui.LogInPanel;

import javax.swing.JFrame;
import javax.swing.JPanel;

import network.ClientConnection;

/**
 * An implementation of Session which stays connected until closed
 */
public class PCSession extends Session {

	private JFrame frame;
	private JPanel currentPanel;
	private ClientConnection connection;
	
	public PCSession() {
	}
	
	public void init() {
		session = this;
		connection = new ClientConnection();
	}
	
	/**
	 * This method starts the GUI by displaying the log in screen
	 */
	public void startGUI() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
//		currentPanel = new LogInPanel(); 
		frame.add(currentPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setSize(frame.getWidth() + 100, frame.getHeight() + 100);
	}
	
	/**
	 * Checks if this PCSession's user's connection is connected to the server
	 * @return	True if connected, false if not
	 */
	public boolean isConnected() {
		return connection.isConnected();
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
	@Override
	public String receive() throws IOException {
		return connection.receive();
	}
	
	/**
	 * Adds the given JPanel to this PCSession's JFrame
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
	 * Fixes this PCSession's JFrame by repainting it, packing it, moving it to the middle and setting it's size by adding some space
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

	@Override
	public String sendAndReceive(String msg) throws IOException {
		send(msg);
		return receive();
	}
	
}
