package gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;

import logic.Debt;
import logic.User;

import session.Session;

public class DebtListPanel extends JPanel{

	private JTable table;
	
	public DebtListPanel() {
		String[] columnNames = {"From" , "To", "What", "Amount", "Comment", "Pending/Confirmed", "Edit?", "Done?"};
//		String[][] rowData = new String[Session.session.getUser().getDebts().getNumberOfTotalDebts()][columnNames.length];
		List<Object[]> debts = new ArrayList<Object[]>();
		for (int i = 0; i < Session.session.getUser().getDebts().getNumberOfPendingDebts(); i++) {
			Debt d = Session.session.getUser().getDebts().getPendingDebt(i);
			Object[] t = {d.getFrom().getUsername(), d.getTo().getUsername(), d.getWhat(), d.getAmount(), d.getComment(), "Pending", new JButton("Edit"), new JButton("Done")};
			debts.add(t);
		}
		table = new JTable((Object[][]) debts.toArray(), columnNames);
		this.add(table);
	}
	
	public static void main(String[] args) {
		User u = new User("Stian", "123");
		User u2 = new User("Arne", "qazqaz");
		u.addDebt(new Debt(100, "NOK", u2, u, "Test"));
		Session.session.setUser(u);
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.add(new DebtListPanel());
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setSize(frame.getWidth() + 100, frame.getHeight() + 100);
	}
}
