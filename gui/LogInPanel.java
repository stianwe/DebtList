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

public class LogInPanel extends JPanel {

	private GridBagConstraints c;
	private JTextField usernameField, passwordField;
	private JButton logInButton;
	
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
		logInButton = new JButton("Log in");
		add(logInButton, c);
		
		// Add listener
		LogInHandler handler = new LogInHandler();
		usernameField.addActionListener(handler);
		passwordField.addActionListener(handler);
		logInButton.addActionListener(handler);
	}
	
	class LogInHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(new LogInPanel());
		f.setVisible(true);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setSize(f.getWidth() + 100, f.getHeight() + 100);
	}
}
