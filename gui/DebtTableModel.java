package gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import logic.Debt;

public class DebtTableModel extends AbstractTableModel{

	public static final int AMOUNT = 0, WHAT = 1, COMMENT = 2, FROM = 3, TO = 4, REQUESTED_BY = 5;
	
	private List<Debt> debts;
	
	public DebtTableModel(List<Debt> debts) {
		this.debts = debts;
	}
	
	@Override
	public boolean isCellEditable(int x, int y) {
		return false;
	}
	
	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public int getRowCount() {
		return debts.size();
	}

	public Debt getDebt(int i) {
		return debts.get(i);
	}
	
	@Override
	public String getColumnName(int i) {
		switch(i) {
		case 0:
			return "Amount";
		case 1:
			return "What";
		case 2:
			return "Comment";
		case 3:
			return "From";
		case 4:
			return "To";
		case 5:
			return "Requested by";
		default:
			throw new RuntimeException("ERROR with DebtTableModel");
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		Debt d = debts.get(row);
		switch(col) {
		case AMOUNT:
			return d.getAmount();
		case WHAT:
			return d.getWhat();
		case COMMENT:
			return d.getComment();
		case FROM:
			return d.getFrom().getUsername();
		case TO:
			return d.getTo().getUsername();
		case REQUESTED_BY:
			return d.getRequestedBy().getUsername();
		default:
			throw new RuntimeException("SOMETHING WENT WRONG WITH THE DebtTableModel!");
		}
	}

}
