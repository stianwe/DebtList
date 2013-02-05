package android.debtlistandroid;

import java.io.IOException;

import requests.CreateUserRequest;
import requests.xml.XMLSerializable;
import session.Session;
import console.Main;
import mail.EmailUtils;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class CreateUserActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_user);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_create_user, menu);
//		return true;
//	}

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

	public void create_user(View v) {
		System.out.println("CREATE USER");
		EditText username = (EditText) findViewById(R.id.create_user_username),
				email = (EditText) findViewById(R.id.create_user_email),
				password1 = (EditText) findViewById(R.id.create_user_password1),
				password2 = (EditText) findViewById(R.id.create_user_password2);
		TextView errorView = (TextView) findViewById(R.id.create_user_error_text);
		// Check that all fields are filled out
		if(!CreateDebtActivity.isFilled(username) || !CreateDebtActivity.isFilled(email) || !CreateDebtActivity.isFilled(password1)
				|| !CreateDebtActivity.isFilled(password2)) {
			// Display error message
			errorView.setText(v.getResources().getString(R.string.create_user_error_fill));
			return;
		}
		// Check that the passwords match
		if(!password1.getText().toString().equals(password2.getText().toString())) {
			// Display error message
			errorView.setText(v.getResources().getString(R.string.create_user_error_password_match));
			return;
		}
		// Check email
		if(!EmailUtils.verifyEmail(email.getText().toString())) {
			// Display error message
			errorView.setText(v.getResources().getString(R.string.create_user_error_invalid_email));
			return;
		}
		// Send request to create user
		try {
			CreateUserRequest response = (CreateUserRequest) XMLSerializable.toObject(Session.session.sendAndReceive(new CreateUserRequest(username.getText().toString(), password1.getText().toString(), email.getText().toString()).toXML()));
			switch(response.getStatus()) {
			case ACCEPTED:
				// Send user to activate view
				break;
			case EMAIL_ALREADY_REGISTERED:
				errorView.setText(v.getResources().getString(R.string.create_user_error_email_already_registered));
				break;
			case INVALID_USERNAME:
				errorView.setText(v.getResources().getString(R.string.create_user_error_invalid_username));
				break;
			case UNHANDLED:
				errorView.setText(v.getResources().getString(R.string.create_user_error_network));
				break;
			case USERNAME_ALREADY_TAKEN:
				errorView.setText(v.getResources().getString(R.string.create_user_error_username_taken));
				break;
			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
			errorView.setText(v.getResources().getString(R.string.create_user_error_network));
		}
	}
	
}
