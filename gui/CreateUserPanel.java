package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import requests.CreateUserRequest;
import requests.XMLParsable;
import session.Session;

public class CreateUserPanel extends JPanel {
	
	private JTextField usernameField, passwordField1, passwordField2;
	private JButton registerButton;
	private JPanel prevPanel;

	public CreateUserPanel(JPanel openedFrom) {
		super(new GridBagLayout());
		this.prevPanel = openedFrom;
		GridBagConstraints c = new GridBagConstraints();
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
		passwordField1 = new JPasswordField(20);
		add(passwordField1, c);
		c.gridx--;
		c.gridy++;
		add(new JLabel("Repeat password: "), c);
		c.gridx++;
		passwordField2 = new JPasswordField(20);
		add(passwordField2, c);
		c.gridy++;
		c.anchor = GridBagConstraints.EAST;
		registerButton = new JButton("Register");
		add(registerButton, c);
		registerButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(!usernameField.getText().equals("") && !passwordField1.getText().equals("") && passwordField1.getText().equals(passwordField2.getText())) {
					Session.session.send(new CreateUserRequest(usernameField.getText(), passwordField1.getText()).toXml());
					try {
						CreateUserRequest cur = (CreateUserRequest) XMLParsable.toObject(Session.session.receive());
						if(cur.isApproved()) {
							System.out.println("User created!");
							Session.session.addPanel(prevPanel);
						} else {
							System.out.println("Username already taken!");
						}
					} catch (Exception e) {
						System.out.println("Corrupted response from server!");
					}
				} else {
					System.out.println("ERROR: Check your spelling!");
				}
			}
		});
	}
	
	
}
