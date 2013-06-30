package android.debtlistandroid;

import java.io.IOException;

import mail.EmailUtils;
import requests.CreateUserRequest;
import requests.xml.XMLSerializable;
import session.Session;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.utils.Tools;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class CreateUserActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_user_activity2);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_create_user_activity2, menu);
//		return true;
//	}

	public void create_user(View v) {
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
			errorView.setVisibility(View.VISIBLE);
			return;
		}
		// Check that the passwords match
		if(!password1.getText().toString().equals(password2.getText().toString())) {
			// Display error message
			errorView.setText(v.getResources().getString(R.string.create_user_error_password_match));
			errorView.setVisibility(View.VISIBLE);
			return;
		}
		// Check email
		if(!EmailUtils.verifyEmail(email.getText().toString())) {
			// Display error message
			errorView.setText(v.getResources().getString(R.string.create_user_error_invalid_email));
			errorView.setVisibility(View.VISIBLE);
			return;
		}
		// Send request to create user
		try {
			CreateUserRequest response = (CreateUserRequest) XMLSerializable.toObject(Session.session.sendAndReceive(new CreateUserRequest(username.getText().toString(), password1.getText().toString(), email.getText().toString()).toXML()));
			System.out.println("Received response with status: " + response.getStatus());
			switch(response.getStatus()) {
			case ACCEPTED:
				System.out.println("USER REGISTERED!!!!!!");
				// Send user to activate view
				ActivateUserActivity.setLoginInformation(username.getText().toString(), password1.getText().toString());
				startActivity(new Intent(this, ActivateUserActivity.class));
				break;
			case EMAIL_ALREADY_REGISTERED:
				errorView.setText(v.getResources().getString(R.string.create_user_error_email_already_registered));
				errorView.setVisibility(View.VISIBLE);
				break;
			case INVALID_USERNAME:
				errorView.setText(v.getResources().getString(R.string.create_user_error_invalid_username));
				errorView.setVisibility(View.VISIBLE);
				System.out.println("INVALID USERNAME, BUT NO EXCEPTION!");
				break;
			case UNHANDLED:
				errorView.setText(v.getResources().getString(R.string.create_user_error_network));
				errorView.setVisibility(View.VISIBLE);
				break;
			case USERNAME_ALREADY_TAKEN:
				errorView.setText(v.getResources().getString(R.string.create_user_error_username_taken));
				errorView.setVisibility(View.VISIBLE);
				break;
			case INCOMPATIBLE_CLIENT_VERSION:
				System.out.println("INCOMPATIBLE VERSION!");
				Tools.displayIncompatibleVersionDialog(this);
				break;
			case COULD_NOT_SEND_WELCOME_MESSAGE:
				System.out.println("Could not send welcome message!");
				errorView.setText(v.getResources().getString(R.string.create_user_error_mail));
				errorView.setVisibility(View.VISIBLE);
			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("EXCEPTION!!!" + e);
			errorView.setText(v.getResources().getString(R.string.create_user_error_network));
			errorView.setVisibility(View.VISIBLE);
		}
	}
}
