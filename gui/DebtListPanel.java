package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;

import logic.Debt;
import logic.User;

import session.Session;

public class DebtListPanel extends JPanel{

	private GridBagConstraints c;
	private JButton plus, minus, pending;
	private Component plusComponent, minusComponent, pendingToComponent, pendingFromComponent;
	private boolean plusIsShowing, minusIsShowing, pendingIsShowing;
	private final int plusYCoord = 1, minusYCoord = 3, pendingYCoord = 5;
	private JLabel pendingToLabel = new JLabel("Pending requests:"), pendingFromLabel = new JLabel("Your pending requests:");
	private ClickListener clickListener = new ClickListener();
	private JTextArea commentField;
	private JTable selectedTable;
	private List<Debt> confirmedDebtsFromMe, confirmedDebtsToMe, pendingDebtsFromMe, pendingDebtsToMe;
	private JButton accept, decline;
	
	public DebtListPanel() {
		super(new GridBagLayout());
		selectedTable = null;
		plusIsShowing = false;
		minusIsShowing = false;
		pendingIsShowing = false;
		
		confirmedDebtsFromMe = new ArrayList<Debt>();
		confirmedDebtsToMe = new ArrayList<Debt>();
		pendingDebtsToMe = new ArrayList<Debt>();
		pendingDebtsFromMe = new ArrayList<Debt>();
		
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridwidth = 2;
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
		pending = new JButton("Pending (" + Session.session.getUser().getNumberOfWaitingDebts() + ")");
		pending.setContentAreaFilled(false);
		pending.setBorderPainted(false);
		pending.setFocusable(false);
		pending.setFont(new Font(null, Font.PLAIN, 15));
		add(pending, c);
		
		c.gridy += 6;
		c.gridx = 0;
		add(new JLabel("Comment:"), c);
		c.gridy++;
		commentField = new JTextArea(6, 25);
		commentField.setEditable(false);
		commentField.setBackground(this.getBackground());
		add(commentField, c);
		
		ButtonListener listener = new ButtonListener(Session.session.getUser());
		plus.addActionListener(listener);
		minus.addActionListener(listener);
		pending.addActionListener(listener);
		
		JPanel p = new JPanel();
		accept = new JButton("Accept");
		c.gridy = 7;
		p.add(accept);
		decline = new JButton("Decline");
		p.add(decline);
		add(p, c);
		accept.setVisible(false);
		decline.setVisible(false);
		accept.setEnabled(false);
		ActionListener al = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Debt selectedDebt = ((DebtTableModel) selectedTable.getModel()).getDebt(selectedTable.getSelectedRow());
				System.out.println(selectedDebt.getAmount() + " " + selectedDebt.getWhat());
				selectedDebt.setIsConfirmed(e.getSource() == accept);
				// TODO: Verify???!!!!?!??!?!?!?!?
				Session.session.send(selectedDebt.toXml());
			}
		};
		decline.addActionListener(al);
		accept.addActionListener(al);
		decline.setEnabled(false);
	}
	
	class ClickListener implements MouseListener {
		@Override
		public void mouseReleased(MouseEvent e) {
			if(pendingToComponent instanceof JScrollPane && e.getSource() == getTable(pendingToComponent)) {
				accept.setEnabled(true);
				decline.setEnabled(true);
			} else {
				accept.setEnabled(false);
				decline.setEnabled(false);
			}
			if(plusComponent instanceof JScrollPane && e.getSource() == getTable(plusComponent)) {
				selectedTable = getTable(plusComponent);
				if(minusComponent instanceof JScrollPane) {
					clearSelection(minusComponent);
				}
				if(pendingToComponent instanceof JScrollPane) {
					clearSelection(pendingToComponent);
				}
				if(pendingFromComponent instanceof JScrollPane) {
					clearSelection(pendingFromComponent);
				}
			} else if(minusComponent instanceof JScrollPane && e.getSource() == getTable(minusComponent)) {
				selectedTable = getTable(minusComponent);
				if(plusComponent instanceof JScrollPane) {
					clearSelection(plusComponent);
				}
				if(pendingToComponent instanceof JScrollPane) {
					clearSelection(pendingToComponent);
				}
				if(pendingFromComponent instanceof JScrollPane) {
					clearSelection(pendingFromComponent);
				}
			} else if(pendingToComponent instanceof JScrollPane && e.getSource() == getTable(pendingToComponent)) {
				selectedTable = getTable(pendingToComponent);
				if(plusComponent instanceof JScrollPane) {
					clearSelection(plusComponent);
				}
				if(minusComponent instanceof JScrollPane) {
					clearSelection(minusComponent);
				}
				if(pendingFromComponent instanceof JScrollPane) {
					clearSelection(pendingFromComponent);
				}
			} else if(pendingFromComponent instanceof JScrollPane && e.getSource() == getTable(pendingFromComponent)) {
				selectedTable = getTable(pendingFromComponent);
				if(plusComponent instanceof JScrollPane) {
					clearSelection(plusComponent);
				}
				if(pendingToComponent instanceof JScrollPane) {
					clearSelection(pendingToComponent);
				}
				if(minusComponent instanceof JScrollPane) {
					clearSelection(minusComponent);
				}
			}
			refreshCommentField();
		}
		@Override
		public void mousePressed(MouseEvent arg0) {}
		@Override
		public void mouseExited(MouseEvent arg0) {}
		@Override
		public void mouseEntered(MouseEvent arg0) {}
		@Override
		public void mouseClicked(MouseEvent e) {}
	}
	
	public void refreshCommentField() {
		commentField.setText((String) selectedTable.getModel().getValueAt(selectedTable.getSelectedRow(), 2));
	}
	
	public static void clearSelection(Component comp) {
		((JTable) ((JViewport) ((JScrollPane) comp).getComponent(0)).getComponent(0)).clearSelection();
		
	}
	
	public static JTable getTable(Component comp) {
		return (JTable) ((JViewport) ((JScrollPane) comp).getComponent(0)).getComponent(0);
	}
	
	class ButtonListener implements ActionListener {

		private User user;
		
		public ButtonListener(User user) {
			this.user = user;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			c.gridx = 0;
			List<String> extraValues = new ArrayList<String>();
			if(e.getSource() == plus) {
				if(plusIsShowing) {
					if(plusComponent instanceof JScrollPane && selectedTable == getTable(plusComponent)) {
						commentField.setText("");
					}
					remove(plusComponent);
					plusIsShowing = false;
				} else {
					extraValues.add("From");
					c.gridy = plusYCoord;
					confirmedDebtsToMe = new ArrayList<Debt>();
					for (int i = 0; i < user.getNumberOfConfirmedDebts(); i++) {
						if(user.getConfirmedDebt(i).getTo() == user) {
							confirmedDebtsToMe.add(user.getConfirmedDebt(i));
						}
					}
					plusIsShowing = true;
					plusComponent = getDebtList(confirmedDebtsToMe, extraValues);
					add(plusComponent, c);
				}
			} else if(e.getSource() == minus) {
				if(minusIsShowing) {
					if(minusComponent instanceof JScrollPane && selectedTable == getTable(minusComponent)) {
						commentField.setText("");
					}
					remove(minusComponent);
					minusIsShowing = false;
				} else {
					extraValues.add("To");
					c.gridy = minusYCoord;
					confirmedDebtsFromMe = new ArrayList<Debt>();
					for (int i = 0; i < user.getNumberOfConfirmedDebts(); i++) {
						if(user.getConfirmedDebt(i).getFrom() == user) {
							confirmedDebtsFromMe.add(user.getConfirmedDebt(i));
						}
					}
					minusIsShowing = true;
					minusComponent = getDebtList(confirmedDebtsFromMe, extraValues);
					add(minusComponent, c);
				}
			} else if(e.getSource() == pending){
				accept.setEnabled(false);
				decline.setEnabled(false);
				if(pendingIsShowing) {
					if((pendingFromComponent instanceof JScrollPane && selectedTable == getTable(pendingFromComponent)) || (pendingToComponent instanceof JScrollPane && selectedTable == getTable(pendingToComponent))) {
						commentField.setText("");
					}
					remove(pendingToComponent);
					remove(pendingFromComponent);
					remove(pendingFromLabel);
					remove(pendingToLabel);
					pendingIsShowing = false;
				} else {
					extraValues.add("From");
					extraValues.add("To");
					c.gridy  = pendingYCoord;
					pendingDebtsFromMe = new ArrayList<Debt>();
					pendingDebtsToMe = new ArrayList<Debt>();
					for (int i = 0; i < user.getNumberOfPendingDebts(); i++) {
						if(user.getPendingDebt(i).getRequestedBy() == user) {
							pendingDebtsToMe.add(user.getPendingDebt(i));
						} else {
							pendingDebtsFromMe.add(user.getPendingDebt(i));
						}
					}
					pendingIsShowing = true;
					add(pendingToLabel, c);
					c.gridy++;
					pendingToComponent = getDebtList(pendingDebtsFromMe, extraValues);
					add(pendingToComponent, c);
					c.gridy += 2;
					add(pendingFromLabel, c);
					c.gridy++;
					pendingFromComponent = getDebtList(pendingDebtsToMe, extraValues);
					add(pendingFromComponent, c);
				}
				if(!(pendingToComponent instanceof JLabel)) {
					accept.setVisible(pendingIsShowing);
					decline.setVisible(pendingIsShowing);
				}
			}
//			repaint();
			Session.session.fixFrame();
		}
		
	}
	
	public Component getDebtList(List<Debt> debts, List<String> extraValues) {
		if(debts.isEmpty()) {
			return new JLabel("None");
		} else {
//			String[] columnNames = {"What", "Amount"/*, "Edit?", "Done?"*/};
//			String[] columnNames = new String[3 + extraValues.size()];
//			columnNames[2] = "Comment";
//			columnNames[1] = "What";
//			columnNames[0] = "Amount";
//			for (int i = 0; i < extraValues.size(); i++) {
//				columnNames[i + 3] = extraValues.get(i);
//			}
			
			JTable table = new JTable(new DebtTableModel(debts));
			
//			Object[][] rowData = new Object[debts.size()][columnNames.length];
//			for (int i = 0; i < debts.size(); i++) {
//				Debt d = debts.get(i);
//				rowData[i][1] = d.getWhat();
//				rowData[i][0] = d.getAmount();
//				rowData[i][2] = d.getComment();
//				if(extraValues.size() == 2) {
//					rowData[i][3] = d.getFrom().getUsername();
//					rowData[i][4] = d.getTo().getUsername();
//				} else {
//					rowData[i][3] = extraValues.get(i).equals("From") ? d.getFrom().getUsername() : d.getTo().getUsername();
//				}
//			}
//			JTable table = new JTable(new DefaultTableModel(rowData, columnNames) {
//				@Override
//				public boolean isCellEditable(int row, int column) {
//					return false;
//				}
//			});
			table.setBackground(this.getBackground());
			table.addMouseListener(clickListener);
			table.removeColumn(table.getColumnModel().getColumn(2));
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//			table.setEnabled(false);
			JScrollPane scrollPane = new JScrollPane(table);
			scrollPane.setPreferredSize(new Dimension(400, debts.size() < 8 ? table.getRowHeight()*(1 + debts.size()) + 4: table.getRowHeight()*9));
			scrollPane.setBorder(BorderFactory.createEmptyBorder());
			return scrollPane;
		}
	}
	
	public static void main(String[] args) {
		User u = new User("Stian", "123");
		User u2 = new User("Arne", "qazqaz");
		u.addPendingDebt(new Debt(0, 100, "NOK", u2, u, "Test", u));
		u.addPendingDebt(new Debt(0, 20, "NOK", u, u2, "Potetgull + brus", u2));
		u.addPendingDebt(new Debt(4, 12, "testers", u2, u, "Testzz", u2));
//		u.addConfirmedDebt(new Debt(0, 2, "Slaps", u, u2, "Test", u));
		u.addConfirmedDebt(new Debt(0, 10, "asd", u2, u, "Test", u));
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
