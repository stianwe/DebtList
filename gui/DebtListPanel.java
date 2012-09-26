package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import logic.Debt;
import logic.User;

import session.Session;

public class DebtListPanel extends JPanel{

	private JTable table;
	private GridBagConstraints c;
	
	public DebtListPanel() {
		super(new GridBagLayout());
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		JButton plus = new JButton("+");
		plus.setForeground(Color.green);
		plus.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
		plus.setBorderPainted(false);
		plus.setFocusable(false);
		plus.setContentAreaFilled(false);
		add(plus,c);
		
		c.gridy++;
		String[] columnNames = {"From" , "To", "What", "Amount", "Comment", "Pending/Confirmed"/*, "Edit?", "Done?"*/};
		Object[][] rowData = new String[Session.session.getUser().getDebts().getNumberOfTotalDebts()][columnNames.length];
		for (int i = 0; i < Session.session.getUser().getDebts().getNumberOfPendingDebts(); i++) {
			Debt d = Session.session.getUser().getDebts().getPendingDebt(i);
			rowData[i][0] = d.getFrom().getUsername();
			rowData[i][1] = d.getTo().getUsername();
			rowData[i][2] = d.getWhat();
			rowData[i][3] = d.getAmount() + "";
			rowData[i][4] = d.getComment();
			rowData[i][5] = "Pending";
//			rowData[i][6] = new JButton("Edit");
//			rowData[i][7] = new JButton("Done");
		}
		table = new JTable(rowData, columnNames);
		table.setBackground(this.getBackground());
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(400, 150));
//		scrollPane.setMaximumSize(new Dimension(20, 20));
//		table.setFillsViewportHeight(true);
		add(scrollPane, c);
//		add(table, c);

		c.gridy++;
		JButton minus = new JButton("-");
		minus.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
		minus.setForeground(Color.red);
		minus.setContentAreaFilled(false);
		minus.setBorderPainted(false);
		minus.setFocusable(false);
		add(minus, c);
		
		c.gridy++;
		JButton pending = new JButton("Pending (" + Session.session.getUser().getDebts().getNumberOfPendingDebts() + ")");
		pending.setContentAreaFilled(false);
		pending.setBorderPainted(false);
		pending.setFocusable(false);
		pending.setFont(new Font(null, Font.PLAIN, 15));
		add(pending, c);
		
	}
	
	public JScrollPane getDebtList(D) {
		
	}
	
	public static void main(String[] args) {
		User u = new User("Stian", "123");
		User u2 = new User("Arne", "qazqaz");
		u.addDebt(new Debt(100, "NOK", u2, u, "Test"));
		u.addDebt(new Debt(20, "NOK", u, u2, "Potetgull + brus"));
		u.addDebt(new Debt(2, "Slaps", u, u2, "Test"));
		Session.session.setUser(u);
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.add(new DebtListPanel());
		frame.pack();
		frame.setLocationRelativeTo(null);
//		frame.setSize(frame.getWidth() + 100, frame.getHeight() + 100);
	}
}
