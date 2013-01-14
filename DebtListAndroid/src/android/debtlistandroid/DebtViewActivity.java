package android.debtlistandroid;

import java.util.ArrayList;
import java.util.List;

import session.Session;

import logic.Debt;
import logic.DebtStatus;
import logic.User;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DebtViewActivity extends ListActivity {

	private DebtAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_debt_view);
		
		// Set up list view
		// Add some example debts
		Session.session.getUser().addConfirmedDebt(new Debt(4, 12.0, "pokes", Session.session.getUser(), new User("Arne"), "Test", Session.session.getUser(), DebtStatus.CONFIRMED));
		Session.session.getUser().addConfirmedDebt(new Debt(5, 3, "Banana", new User("Arne"), Session.session.getUser(), "Test", Session.session.getUser(), DebtStatus.CONFIRMED));
		Session.session.getUser().addConfirmedDebt(new Debt(6, 20, "kr", new User("Arne"), Session.session.getUser(), "Test", Session.session.getUser(), DebtStatus.CONFIRMED));
		adapter = new DebtAdapter(this, Session.session.getUser().getConfirmedDebts());
		setListAdapter(adapter);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_debt_view, menu);
//		return true;
//	}

	
	class DebtAdapter extends ArrayAdapter<Debt> {

		private List<Debt> debts;
		private Context context;
		
		public DebtAdapter(Context context, List<Debt> debts) {
			super(context, R.layout.activity_debt_view, debts);
			this.context = context;
			// Encapsulate the list properly
			this.debts = new ArrayList<Debt>();
			for (Debt d : debts) {
				this.debts.add(d);
			}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Debt d = debts.get(position);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.activity_debt_view, null);
			TextView topTextView = (TextView) view.findViewById(R.id.toptext);
			TextView bottomTextView = (TextView) view.findViewById(R.id.bottomtext);
			topTextView.setText(d.getAmount() + " " + d.getWhat());
			bottomTextView.setText((d.getFrom().equals(Session.session.getUser()) ? d.getTo() : d.getFrom()).getUsername());
			view.setBackgroundColor((d.getFrom().equals(Session.session.getUser()) ? Color.rgb(204, 0, 0) : Color.rgb(0, 204, 102)));
			return view;
		}
		
	}
}
