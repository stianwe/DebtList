package android.debtlistandroid;

import java.util.ArrayList;
import java.util.List;

import console.Main;

import session.Session;

import logic.Debt;
import logic.DebtStatus;
import logic.User;
import android.opengl.Visibility;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class DebtViewActivity extends ListActivity {

	private DebtAdapter adapter;
	private Debt selectedDebt = null;
	// Only for testing purposes
	private static boolean exampleDebtsAdded = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_debt_view);
		
		// Set up list view
		if(!exampleDebtsAdded) {
			// Add some example debts
	//		Session.session.getUser().addConfirmedDebt(new Debt(44, 12.0, "pokes", Session.session.getUser(), new User("Arne"), "Test", Session.session.getUser(), DebtStatus.CONFIRMED));
	//		Session.session.getUser().addConfirmedDebt(new Debt(55, 3, "Banana", new User("Arne"), Session.session.getUser(), "Test", Session.session.getUser(), DebtStatus.CONFIRMED));
	//		Session.session.getUser().addConfirmedDebt(new Debt(66, 20, "kr", new User("Arne"), Session.session.getUser(), "Test", Session.session.getUser(), DebtStatus.CONFIRMED));
//			Session.session.getUser().addPendingDebt(new Debt(77, 3, "kr", new User("Arne"), Session.session.getUser(), "TestPending", Session.session.getUser(), DebtStatus.REQUESTED));
//			Session.session.getUser().addPendingDebt(new Debt(88, 3, "kr", Session.session.getUser(), new User("Arne"), "TestPending", Session.session.getUser(), DebtStatus.REQUESTED));
			exampleDebtsAdded = true;
		}
		List<List<Debt>> debts = new ArrayList<List<Debt>>();
		// TODO: Check if we have any debts at all
		debts.add(Session.session.getUser().getConfirmedDebts());
		debts.add(Session.session.getUser().getPendingDebts());
		List<String> separators = new ArrayList<String>();
		separators.add("Confirmed debts");
		separators.add("Pending debts");
		adapter = new DebtAdapter(this, debts, separators, constructNullList(debts.get(0).size() + debts.get(1).size()));
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_debt_view, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_create_debt) {
			startActivity(new Intent(this, CreateDebtActivity.class));
		} else if(item.getItemId() == R.id.menu_friends) {
			startActivity(new Intent(this, FriendViewActivity.class));
		} else if(item.getItemId() == R.id.menu_settings) {
			System.out.println("SETTINGS!");
		} else {
			System.out.println("WTF?!");
		}
		return true;
	}
	
	public void complete_debt(View v) {
		Log.d("ARNE", "COMPLETE DEBT: " + selectedDebt.toString());
		Main.processCommand("complete debt " + selectedDebt.getId());
		recreate();
	}
	
	public void accept_debt(View v) {
		String command = "accept debt " + selectedDebt.getId();
		Main.processCommand(command);
		recreate();
	}
	
	public void decline_debt(View v) {
		Log.d("ARNE", "DECLINE DEBT: " + selectedDebt.toString());
		Main.processCommand("decline debt " + selectedDebt.getId());
		recreate();
	}
	
	/**
	 * Constructs and returns a list filled with nulls.
	 * @param size	The size of the list (number of nulls to put in it)
	 * @return		The list containing the nulls
	 */
	private static List<Debt> constructNullList(int size) {
		List<Debt> list = new ArrayList<Debt>();
		for (int i = 0; i < size; i++) {
			list.add(null);
		}
		return list;
	}
	
	class DebtAdapter extends ArrayAdapter<Debt> {

		private List<List<Debt>> debtLists;
		private Context context;
		private List<String> separatorTexts;
		private View lastExpandedView = null;
		
		public DebtAdapter(Context context, List<List<Debt>> debtLists, List<String> separatorTexts, List<Debt> nullList) {
			super(context, R.layout.activity_debt_view, nullList);
			this.context = context;
			this.separatorTexts = separatorTexts;
			this.debtLists = debtLists;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View view = inflater.inflate(R.layout.activity_debt_view, null);
			TextView sep = (TextView) view.findViewById(R.id.separator);
			// Find the corresponding debt
			Debt d = null;
			int count = 0; // The number of debts 'before' the current list
			int listIndex = -1;
			for (int i = 0; i < debtLists.size(); i++) {
				List<Debt> ds = debtLists.get(i);
				if(position >= ds.size() + count) {
					count += ds.size();
				} else {
					d = ds.get(position - count);
					listIndex = i;
					break;
				}
			}
			if (position == count) {
				// Add separator if on top of list
				sep.setVisibility(View.VISIBLE);
				sep.setText(separatorTexts.get(listIndex));
				sep.setBackgroundColor(Color.BLACK);
				sep.setTextColor(Color.WHITE);
			} else {
				// Else remove the space
				sep.setHeight(0);
			}
			// Fix debt text
			TextView topTextView = (TextView) view.findViewById(R.id.toptext);
			TextView bottomTextView = (TextView) view.findViewById(R.id.bottomtext);
			topTextView.setText(d.getAmount() + " " + d.getWhat());
			bottomTextView.setText((d.getFrom().equals(Session.session.getUser()) ? d.getTo() : d.getFrom()).getUsername());
			TextView comment = (TextView) view.findViewById(R.id.commenttext);
			comment.setText(d.getComment());
			// Collapse this view
			collapse(view);
			// Set the background color
			view.setBackgroundColor((d.getFrom().equals(Session.session.getUser()) ? Color.rgb(204, 0, 0) : Color.rgb(0, 204, 102)));
			// Expand on click
//			final boolean isConfirmed = listIndex == 0;
			final Debt debt = d;
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(lastExpandedView == view) {
						collapse(lastExpandedView);
						lastExpandedView = null;
						return;
					}
					if(lastExpandedView != null) {
						collapse(lastExpandedView);
					}
//					expand(v, isConfirmed);
					expand(v, debt);
					lastExpandedView = v;
					selectedDebt = debt;
				}
			});
			return view;
		}
		
		public void expand(View view, Debt d) {
			((TextView) view.findViewById(R.id.commenttext)).setVisibility(View.VISIBLE);
			if(d.isConfirmed()) {
				// Check that this user already haven't marked this debt as completed
				if((d.getFrom().equals(Session.session.getUser()) && d.getStatus() != DebtStatus.COMPLETED_BY_FROM) ||
						(d.getTo().equals(Session.session.getUser()) && d.getStatus() != DebtStatus.COMPLETED_BY_TO)) {
					((Button) view.findViewById(R.id.button_complete)).setVisibility(View.VISIBLE);
				}
			} else {
				// Check that this is not the user that requested the debt
				if(!d.getRequestedBy().equals(Session.session.getUser())) {
					((Button) view.findViewById(R.id.button_accept)).setVisibility(View.VISIBLE);
					((Button) view.findViewById(R.id.button_decline)).setVisibility(View.VISIBLE);
				}
			}
		}
		
		public void collapse(View view) {
			((TextView) view.findViewById(R.id.commenttext)).setVisibility(View.GONE);
			((Button) view.findViewById(R.id.button_accept)).setVisibility(View.GONE);
			((Button) view.findViewById(R.id.button_complete)).setVisibility(View.GONE);
			((Button) view.findViewById(R.id.button_decline)).setVisibility(View.GONE);
		}
		
	}
}
