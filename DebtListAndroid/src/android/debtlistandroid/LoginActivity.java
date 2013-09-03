package android.debtlistandroid;

import network.Constants;
import requests.LogInRequestStatus;
import session.Session;
import session.Updater;
import utils.PasswordHasher;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.sessionX.AndroidSession;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.updater.AndroidUpdater;
import android.util.Log;
import android.utils.Tools;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

	public static Context context;
	public static View view;
	
	private View loginErrorTextView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		// Register and validate SAX driver
		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
		
		// Check if we are logged in
		if(Session.session != null && Session.session.isLoggedIn()) {
			// Send user to debt view.. No need to show login view
			startActivity(new Intent(this, DebtViewActivity.class));
			return;
		}
		
		// Start the session
		new AndroidSession().init();
		Session.session.connect(Constants.SERVER_ADDRESS, Constants.STANDARD_SERVER_PORT);
		
		loginErrorTextView = findViewById(R.id.loginerrortext);
		
		context = this;
		
		// Check if user identification is saved on the phone
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String username = sharedPref.getString(getString(R.string.persistent_identification_username_key), null);
		System.out.println("Loaded peristent user name: " + username);
		String passwordHash = sharedPref.getString(getString(R.string.persistent_identification_password_hash_key), null);
		System.out.println("Loaded persistent password hash: " + passwordHash);
		if (username != null && passwordHash != null) {
			LogInRequestStatus logInStatus = Session.session.logInWithHashedPassword(username, passwordHash);
			processLogInRequestResponse(logInStatus, findViewById(R.id.login_activity_layout));
		}
	}
	
// No need for menu on the login screen(?)
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_login, menu);
//		return true;
//	}

	public void register(View v) {
		view = v;
		// Clear session user in case someone has gotten to this activity while logged in
		Session.session.clear();
		startActivity(new Intent(this, CreateUserActivity.class));
	}
	
	public void login(View v) {
		view = v;
		// Clear session user in case someone has gotten to this activity while logged in
		Session.session.clear();
		LogInRequestStatus loginRequestStatus = Session.session.logIn(((EditText) findViewById(R.id.edit_username)).getText().toString(), ((EditText) findViewById(R.id.edit_password)).getText().toString());
		processLogInRequestResponse(loginRequestStatus, v);
	}
	
	public void processLogInRequestResponse(LogInRequestStatus responseStatus, View vv) {
		switch(responseStatus) {
		case ACCEPTED:
			// Store the log in information
			SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
			String password = ((AndroidSession) Session.session).getPassword();
			if (password != null) {
				prefsEditor.putString(getString(R.string.persistent_identification_password_hash_key), PasswordHasher.hashPassword(password));
				prefsEditor.putString(getString(R.string.persistent_identification_username_key), Session.session.getUser().getUsername());
				prefsEditor.commit();
			}
			// Start the updater
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			System.out.println("Loaded time between updates: " + prefs.getLong(
									getString(R.string.settings_time_between_updates_key), -1));
			((AndroidSession) Session.session).startUpdater(this, 
					prefs.getLong(
							getString(R.string.settings_time_between_updates_key), 
							prefs.getLong(
									getString(R.string.settings_time_between_updates_key),
									Constants.STANDARD_TIME_BETWEEN_UPDATES)), 
							!prefs.getBoolean(
									getString(R.string.settings_disable_updates_when_not_on_wifi_key), 
									!Constants.STANDARD_DISABLE_UPDATES_WHEN_NOT_ON_WIFI));
			// Start the DebtViewActivity
			Intent intent = new Intent(this, DebtViewActivity.class);
			startActivity(intent);
			break;
		case ALREADY_LOGGED_ON:
			// Should not happen and can be removed!
			System.err.println("ERROR!!!!!!!!!!!!!!!!!");
			break;
		case UNHANDLED:
			// Probably connection error
			((TextView) loginErrorTextView).setText(getString(R.string.login_error_connection));
			loginErrorTextView.setVisibility(View.VISIBLE);
			break;
		case WRONG_INFORMATION:
			((TextView) loginErrorTextView).setText(getString(R.string.login_error_incorrect));
			loginErrorTextView.setVisibility(View.VISIBLE);
			// Display outdated version dialog if outdated version
			// TODO: REMOVE TRUE
			if(Session.session.isVersionOutdated()) {
				final Context diss = this;
				vv.post(new Runnable() {
					public void run() {
						Tools.displayOutdatedVersionDialog(diss);
					}
				});
				// We only want the message displayed once
				Session.session.setIsVersionOutdated(false);
			}
			break;
		case NOT_ACTIVATED:
			// Send user to activation view
			ActivateUserActivity.setLoginInformation(((EditText)findViewById(R.id.edit_username)).getText().toString(), ((EditText) findViewById(R.id.edit_password)).getText().toString());
			startActivity(new Intent(this, ActivateUserActivity.class));
			break;
		case INCOMPATIBLE_CLIENT_VERSION:
			// TODO: Display information about incompatible version
			System.out.println("INCOMPATIBLE VERSION!");
			Tools.displayIncompatibleVersionDialog(this);
			break;
		default:
			break;
		}
	}
}
