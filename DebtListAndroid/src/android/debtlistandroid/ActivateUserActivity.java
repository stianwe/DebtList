package android.debtlistandroid;

import network.Constants;
import session.Session;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.sessionX.AndroidSession;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ActivateUserActivity extends Activity {

	private static String username, password;
	
	public static void setLoginInformation(String username, String password) {
		ActivateUserActivity.username = username;
		ActivateUserActivity.password = password;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activate_user);
	}

	public void activate_user(View v) {
		System.out.println("ACTIVATE USER!");
		EditText key = (EditText) findViewById(R.id.activate_user_key);
//		EditText username = (EditText) findViewById(R.id.activate_user_username),
//				password = (EditText) findViewById(R.id.activate_user_password),
		// Fetch login information from previous activity
		// Check that all fields are filled
		if(/*!CreateDebtActivity.isFilled(username) || !CreateDebtActivity.isFilled(password) ||*/ !CreateDebtActivity.isFilled(key)) {
			setErrorText(v.getResources().getString(R.string.activate_user_error_filled));
		}
//		switch(Session.session.logIn(username.getText().toString(), password.getText().toString(), key.getText().toString())) {
		switch(Session.session.logIn(username, password, key.getText().toString())) {
		case ACCEPTED:
			// Activation and login ok, proceed
			// Start update requester
			((AndroidSession) Session.session).startUpdater(this, getPreferences(Context.MODE_PRIVATE).getLong(getString(R.string.settings_time_between_updates_key), Constants.STANDARD_TIME_BETWEEN_UPDATES), !getPreferences(Context.MODE_PRIVATE).getBoolean(getString(R.string.settings_disable_updates_when_not_on_wifi_key), !Constants.STANDARD_DISABLE_UPDATES_WHEN_NOT_ON_WIFI));
			// Send user to debt list view
			startActivity(new Intent(this, DebtViewActivity.class));
			break;
		case ALREADY_LOGGED_ON:
			// TODO: SHOULD NOT HAPPEN!
			setErrorText("ALREADY LOGGED ON: THIS SHOULD NOT HAPPEN!");
			break;
		case INVALID_ACTIVATION_KEY:
			// Display error message
			setErrorText(v.getResources().getString(R.string.activate_user_error_key));
			break;
		case NOT_ACTIVATED:
			// TODO: SHOULD NOT HAPPEN!
			setErrorText("User was not activated when trying to activate it.. This should not happen!");
			break;
		case UNHANDLED:
			// Display error message
			setErrorText(v.getResources().getString(R.string.activate_user_error_network));
			break;
		case WRONG_INFORMATION:
			// Display error message
			setErrorText(v.getResources().getString(R.string.activate_user_error_login_info));
			break;
		default:
			break;
		}
	}

	private void setErrorText(String text) {
		TextView tv = (TextView) findViewById(R.id.activate_user_error);
		tv.setText(text);
		tv.setVisibility(View.VISIBLE);
	}
}
