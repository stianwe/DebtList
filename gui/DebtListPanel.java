package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import logic.Debt;
import logic.User;

import session.Session;

public class DebtListPanel extends JPanel{

	private GridBagConstraints c;
	private JButton plus, minus, pending;
	private Component plusTable, minusTable, pendingTable;
	private boolean plusIsShowing, minusIsShowing, pendingIsShowing;
	private final int plusYCoord = 1, minusYCoord = 3, pendingYCoord = 5;
	
	public DebtListPanel() {
		super(new GridBagLayout());
		plusIsShowing = false;
		minusIsShowing = false;
		pendingIsShowing = false;
		
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		plus = new JButton("+");
		plus.setForeground(Color.green);
		plus.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
		plus.setBorderPainted(false);
		plus.setFocusable(false);
		plus.setContentAreaFilled(false);
		add(plus,c);
		
		c.gridy++;
		c.gridy++;
		minus = new JButton("-");
		minus.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
		minus.setForeground(Color.red);
		minus.setContentAreaFilled(false);
		minus.setBorderPainted(false);
		minus.setFocusable(false);
		add(minus, c);
		
		c.gridy++;
		c.gridy++;
		pending = new JButton("Pending (" + Session.session.getUser().getNumberOfPendingDebts() + ")");
		pending.setContentAreaFilled(false);
		pending.setBorderPainted(false);
		pending.setFocusable(false);
		pending.setFont(new Font(null, Font.PLAIN, 15));
		add(pending, c);
		
		ButtonListener listener = new ButtonListener(Session.session.getUser());
		plus.addActionListener(listener);
		minus.addActionListener(listener);
		pending.addActionListener(listener);
	}
	
	class ButtonListener implements ActionListener {

		private User user;
		
		public ButtonListener(User user) {
			this.user = user;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			c.gridx = 0;
			if(e.getSource() == plus) {
				if(plusIsShowing) {
					remove(plusTable);
					plusIsShowing = false;
				} else {
					c.gridy = plusYCoord;
					List<Debt> debts = new ArrayList<Debt>();
					for (int i = 0; i < user.getNumberOfConfirmedDebts(); i++) {
						if(user.getConfirmedDebt(i).getTo() == user) {
							debts.add(user.getConfirmedDebt(i));
						}
					}
					plusIsShowing = true;
					plusTable = getDebtList(debts);
					add(plusTable, c);
				}
			} else if(e.getSource() == minus) {
				if(minusIsShowing) {
					remove(minusTable);
					minusIsShowing = false;
				} else {
					c.gridy = minusYCoord;
					List<Debt> debts = new ArrayList<Debt>();
					for (int i = 0; i < user.getNumberOfConfirmedDebts(); i++) {
						if(user.getConfirmedDebt(i).getFrom() == user) {
							debts.add(user.getConfirmedDebt(i));
						}
					}
					minusIsShowing = true;
					minusTable = getDebtList(debts);
					add(minusTable, c);
				}
			} else if(e.getSource() == pending){
				if(pendingIsShowing) {
					remove(pendingTable);
					pendingIsShowing = false;
				} else {
					c.gridy  = pendingYCoord;
					List<Debt> debts = new ArrayList<Debt>();
					for (int i = 0; i < user.getNumberOfPendingDebts(); i++) {
						debts.add(user.getPendingDebt(i));
					}
					pendingIsShowing = true;
					pendingTable = getDebtList(debts);
					add(pendingTable, c);
				}
			}
//			repaint();
			Session.session.fixFrame();
		}
		
	}
	
	public Component getDebtList(List<Debt> debts) {
		if(debts.isEmpty()) {
			return new JLabel("None");
		} else {
			String[] columnNames = {"What", "Amount"/*, "Edit?", "Done?"*/};
			Object[][] rowData = new Object[debts.size()][columnNames.length];
			for (int i = 0; i < debts.size(); i++) {
				Debt d = debts.get(i);
				rowData[i][0] = d.getWhat();
				rowData[i][1] = d.getAmount();
			}
//			JTable table = new JTable(new DefaultTableModel() {
//				@Override
//				public boolean isCellEditable(int row, int column) {
//					System.out.println("HERRE SUG");
//					return true;
//				}
//			});
//			String[] t = {"hei"};
//			((DefaultTableModel) table.getModel()).addRow(rowData);
			JTable table = new JTable(rowData, columnNames);
//			table.setModel(new DefaultTableModel());
//			table.setModel(new DefaultTableModel() {
//				@Override
//				public boolean isCellEditable(int row, int column) {
//					System.out.println("HERRE SUG");
//					return true;
//				}
//			});
//			table.getModel().isCellEditable(0, 0);
			table.setBackground(this.getBackground());
//			table.setEnabled(false);
			table.setVisible(true);
			JScrollPane scrollPane = new JScrollPane(table);
			scrollPane.setPreferredSize(new Dimension(400, debts.size() < 8 ? table.getRowHeight()*(1 + debts.size()) + 4: table.getRowHeight()*9));
//			scrollPane.setPreferredSize(new Dimension(400, 400));
			scrollPane.setBorder(BorderFactory.createEmptyBorder());
			return scrollPane;
		}
	}
	
	public static void main(String[] args) {
		User u = new User("Stian", "123");
		User u2 = new User("Arne", "qazqaz");
		u.addPendingDebt(new Debt(100, "NOK", u2, u, "Test"));
		u.addPendingDebt(new Debt(20, "NOK", u, u2, "Potetgull + brus"));
		u.addConfirmedDebt(new Debt(2, "Slaps", u, u2, "Test"));
		Session.session.setUser(u);
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.add(new DebtListPanel());
		frame.pack();
		frame.setLocationRelativeTo(null);
//		frame.setSize(frame.getWidth() + 100, frame.getHeight() + 100);
		Session.session.setFrame(frame);
	}
}
