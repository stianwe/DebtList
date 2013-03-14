package android.debtlistandroid;

import session.Session;
import network.Constants;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.sessionX.AndroidSession;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		// Load settings
		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		// Time between updates
		long timeBetweenUpdates = sharedPref.getLong(getString(R.string.settings_time_between_updates_key), Constants.STANDARD_TIME_BETWEEN_UPDATES);
		((TextView) findViewById(R.id.settings_time_between_updates)).setText(timeBetweenUpdates + "");
		// Should update when not on wifi
		((CheckBox) findViewById(R.id.settings_disable_when_not_on_wifi_cb)).setChecked(sharedPref.getBoolean(getString(R.string.settings_disable_updates_when_not_on_wifi_key), Constants.STANDARD_DISABLE_UPDATES_WHEN_NOT_ON_WIFI));
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_settings, menu);
//		return true;
//	}
	
	public void save(View view) {
		SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
		// Check that updates are reasonable
		// Check that the time between updates is no shorter than 1 minute
		try{
			long tbu = Long.parseLong(((EditText) findViewById(R.id.settings_time_between_updates)).getText().toString());
			if(tbu == 0) {
				// Disable automatic updates
				((AndroidSession) Session.session).stopUpdater();
				editor.putLong(getString(R.string.settings_time_between_updates_key), tbu);
			} else if(tbu < 60 * 1000) {
				// Display error message
				new AlertDialog.Builder(this).setMessage(R.string.settings_time_between_updates_error_message).show();
				System.out.println("Too short time interval!");
				return;
			} else {
				// Ok, save the value
				System.out.println("Ok time interval!");
				editor.putLong(getString(R.string.settings_time_between_updates_key), tbu);
			}
		} catch (NumberFormatException e) {
			System.out.println("FAILZ!");
		}
		// Save the disable updates when not on wifi value
		boolean disableUpdatesWhenNotOnWifi = ((CheckBox) findViewById(R.id.settings_disable_when_not_on_wifi_cb)).isChecked();
		editor.putBoolean(getString(R.string.settings_disable_updates_when_not_on_wifi_key), disableUpdatesWhenNotOnWifi);
//		if(...)
//		// Save the changes
//		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
//		editor.putInt(getString(R.string.saved_high_score), newHighScore);
		editor.commit();
		// Make the updater use the new changes
		((AndroidSession) Session.session).startUpdater(this, getPreferences(MODE_PRIVATE).getLong(getString(R.string.settings_time_between_updates_key), Constants.STANDARD_TIME_BETWEEN_UPDATES), !disableUpdatesWhenNotOnWifi);
		// Move the user to the debtview
		startActivity(new Intent(this, DebtViewActivity.class));
	}

}
