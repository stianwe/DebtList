package android.debtlistandroid;

import java.util.ArrayList;
import java.util.List;

import console.Main;

import session.Session;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.text.Editable;

public class CreateDebtActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_debt);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// Set the spinner's adapter
		List<CharSequence> usernames = new ArrayList<CharSequence>();
		for (int i = 0; i < Session.session.getUser().getNumberOfFriends(); i++) {
			usernames.add(Session.session.getUser().getFriend(i).getUsername());
		}
		// Add hint
		usernames.add("Select user");
		Spinner spinner =(Spinner) findViewById(R.id.spinner_create_debt_user); 
		// Use an adapter that will use the last element as hint
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, usernames) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				if(position == getCount()) {
					((TextView)v.findViewById(android.R.id.text1)).setText("");
					((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount()));
				}
				return v;
			}
			
			@Override
			public int getCount() {
				return super.getCount()-1;
			}
		};
		spinner.setAdapter(adapter);
		// Display the hint
		spinner.setSelection(adapter.getCount());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_create_debt, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void create_debt(View v) {
		EditText amount = (EditText) findViewById(R.id.create_debt_amount),
				what = (EditText) findViewById(R.id.create_debt_what),
				comment = (EditText) findViewById(R.id.create_debt_comment);
		RadioButton from = (RadioButton) findViewById(R.id.create_debt_radio_from),
				to = (RadioButton) findViewById(R.id.create_debt_radio_to);
		// Check that all fields are filled
		boolean allFieldsFilled = true;
		// Check that a valid user (not the hint) is selected
		Spinner spinner = (Spinner) findViewById(R.id.spinner_create_debt_user);
		if(spinner.getSelectedItemPosition() == spinner.getCount()) {
			System.out.println("brukernavn");
			allFieldsFilled = false;
		} 
		// Check that one of the radio buttons are checked
		if(!(from.isChecked() || to.isChecked())) {
			allFieldsFilled = false;
			System.out.println("radio");
			if(!from.isChecked()) System.out.println("FROM");
			if(!((RadioButton) findViewById(R.id.create_debt_radio_to)).isChecked()) System.out.println("TO");
		} 
		// Check amount field
		if(!isFilled(amount)) {
			System.out.println("amount");
			allFieldsFilled = false;
		}
		// Check what field
		if(!isFilled(what)) {
			System.out.println("what");
			allFieldsFilled = false;
		}
		// Check comment filed TODO: Should perhaps be made optional?
		if(!isFilled(comment)) {
			System.out.println("comment");
			allFieldsFilled = false;
		}
		// Check that all fields are filled
		TextView errorField = (TextView) findViewById(R.id.create_debt_error_text); 
		if(!allFieldsFilled) {
			errorField.setText(v.getResources().getString(R.string.create_debt_error_message_filled));
			errorField.setVisibility(View.VISIBLE);
		} else {
			String sWhat = what.getText().toString(), sComment = comment.getText().toString();
			// Check that no '"' is present
			if(sWhat.indexOf('"') > -1 || sComment.indexOf('"') > -1) {
				errorField.setText(v.getResources().getString(R.string.create_debt_error_message_invalid));
				errorField.setVisibility(View.VISIBLE);
			} else {
				// Remove error message (not necessary?)
//				errorField.setVisibility(View.INVISIBLE);
				// Build command
				if(from.isChecked()) {
					System.out.println("From was selected");
				} else {
					System.out.println("From was not selected");
				}
				Main.processCreateDebt("create debt " + amount.getText().toString() + " " + '"' + sWhat + '"' + " " + 
						(from.isChecked() ? "from" : "to") + " " + '"' + ((String) spinner.getSelectedItem()) + '"' + " " + 
						'"' + sComment + '"');
				// Return to main view
				startActivity(new Intent(this, DebtViewActivity.class));
			}
		}
	}
	
	public static boolean isFilled(EditText e) {
		return e.getText() != null && e.getText().length() > 0;
	}
}
