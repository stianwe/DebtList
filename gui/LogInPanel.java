package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import requests.LogInRequest;
import requests.LogInRequestStatus;
import requests.XMLParsable;
import session.Session;

import network.ClientConnection;

public class LogInPanel extends JPanel {

	private GridBagConstraints c;
	private JTextField usernameField, passwordField;
	private JButton logInButton, registerButton;
	
	public LogInPanel() {
		super(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		add(new JLabel("Username: "), c);
		c.gridx++;
		usernameField = new JTextField(20);
		add(usernameField, c);
		c.gridx--;
		c.gridy++;
		add(new JLabel("Password: "), c);
		c.gridx++;
		passwordField = new JPasswordField(20);
		add(passwordField, c);
		c.gridy++;
		c.anchor = GridBagConstraints.EAST;
		registerButton = new JButton("Register");
		logInButton = new JButton("Log in");
		JPanel p = new JPanel();
		p.add(registerButton);
		p.add(logInButton);
		add(p, c);
		
		
		// Add listener
		ActionHandler handler = new ActionHandler();
		usernameField.addActionListener(handler);
		passwordField.addActionListener(handler);
		logInButton.addActionListener(handler);
		registerButton.addActionListener(handler);
	}
	
	private LogInPanel dis = this;
	
	class ActionHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			// TODO Auto-generated method stub
			// ARNE
			// con.connect("192.168.1.7", 13337);
			// LOCAL
			if(event.getSource() != registerButton) {
				Session.session.connect("localhost", 13337);
				Session.session.send(new LogInRequest(usernameField.getText(), passwordField.getText()).toXml());
				LogInRequest resp = (LogInRequest)XMLParsable.toObject(Session.session.receive());
				switch(resp.getStatus()) {
				case UNHANDLED:
					System.out.println("LogInRequest was not handled by the server! Something is probably wrong with the connection!");
					break;
				case ACCEPTED:
					System.out.println("Log in OK!");
					Session.session.setUser(resp.getUser());
					break;
				case WRONG_INFORMATION:
					System.out.println("Wrong username/password!");
					break;
				case ALREADY_LOGGED_ON:
					System.out.println("User already logged on!");
					break;
				}
	//			if(resp.isAccepted()) {
	//				System.out.println("LOG IN OK!");
	//			} else {
	//				System.out.println("LOG IN FAILED!");
	//				
	//			}
			} else {
				Session.session.addPanel(new CreateUserPanel(dis));
			}
		}
	}
}
