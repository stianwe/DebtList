package android.debtlistandroid;

import network.Constants;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		// Load settings
		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		long timeBetweenUpdates = sharedPref.getLong(getString(R.string.settings_time_between_updates_key), Constants.STANDARD_TIME_BETWEEN_UPDATES);
		((TextView) findViewById(R.id.settings_time_between_updates)).setText(timeBetweenUpdates + "");
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_settings, menu);
//		return true;
//	}
	
	public void save(View view) {
		// Check that updates are reasonable
//		if(...)
//		// Save the changes
//		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
//		SharedPreferences.Editor editor = sharedPref.edit();
//		editor.putInt(getString(R.string.saved_high_score), newHighScore);
//		editor.commit();
	}

}
