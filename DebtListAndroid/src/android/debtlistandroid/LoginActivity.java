package android.debtlistandroid;

import session.Session;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.sessionX.AndroidSession;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends Activity {

//	public static String EXTRA_USERNAME = "DebtListAndroid.android.debtlistandroid.USERNAME", EXTRA_PASSWORD = "DebtListAndroid.android.debtlistandroid.PASSWORD";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		// Register and validate SAX driver
		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
		
		// Start the session
		new AndroidSession().init();
		Session.session.connect("invert.ed.ntnu.no", 13337);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	public void login(View v) {
		final Activity dis = this;
		new Thread() {
			public void run() {
				switch(Session.session.logIn(((EditText) findViewById(R.id.edit_username)).getText().toString(), ((EditText) findViewById(R.id.edit_password)).getText().toString())) {
				case ACCEPTED:
					// Start the DebtViewActivity
					Intent intent = new Intent(dis, DebtViewActivity.class);
					startActivity(intent);
					break;
				case ALREADY_LOGGED_ON:
					break;
				case UNHANDLED:
					break;
				case WRONG_INFORMATION:
					break;
				default:
					break;
				}
			}
		}.start();
	}
}
