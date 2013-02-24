package android.debtlistandroid;

import network.Constants;
import session.Session;
import session.Updater;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.sessionX.AndroidSession;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

	private View loginErrorTextView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		// Register and validate SAX driver
		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
		
		// Start the session
		new AndroidSession().init();
		Session.session.connect(Constants.SERVER_ADDRESS, Constants.STANDARD_SERVER_PORT);
		
		loginErrorTextView = findViewById(R.id.loginerrortext);
	}
	
// No need for menu on the login screen(?)
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_login, menu);
//		return true;
//	}

	public void register(View v) {
		// Clear session user in case someone has gotten to this activity while logged in
		Session.session.clear();
		startActivity(new Intent(this, CreateUserActivity.class));
	}
	
	public void login(View v) {
		// Clear session user in case someone has gotten to this activity while logged in
		Session.session.clear();
		final Activity dis = this;
		final View vv = v;
//		new Thread() {
//			public void run() {
				switch(Session.session.logIn(((EditText) findViewById(R.id.edit_username)).getText().toString(), ((EditText) findViewById(R.id.edit_password)).getText().toString())) {
				case ACCEPTED:
					// Start the updater
					
					System.out.println("Displaying notification!");
					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
			        .setContentTitle("Updater started!")
			        .setContentText("This is the subject.. :)").setSmallIcon(R.drawable.ic_launcher);
					Intent resultIntent = new Intent(this, DebtViewActivity.class);

					// The stack builder object will contain an artificial back stack for the
					// started Activity.
					// This ensures that navigating backward from the Activity leads out of
					// your application to the Home screen.
					TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
					// Adds the back stack for the Intent (but not the Intent itself)
					stackBuilder.addParentStack(DebtViewActivity.class);
					// Adds the Intent that starts the Activity to the top of the stack
					stackBuilder.addNextIntent(resultIntent);
					PendingIntent resultPendingIntent =
					        stackBuilder.getPendingIntent(
					            0,
					            PendingIntent.FLAG_UPDATE_CURRENT
					        );
					mBuilder.setContentIntent(resultPendingIntent);
					NotificationManager mNotificationManager =
					    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					// mId allows you to update the notification later on.
					mNotificationManager.notify(0, mBuilder.build());
					
					Updater updater = new Updater();
					((AndroidSession) Session.session).startUpdater();
					// Start the DebtViewActivity
					Intent intent = new Intent(dis, DebtViewActivity.class);
					startActivity(intent);
					break;
				case ALREADY_LOGGED_ON:
					// Should not happen and can be removed!
					System.err.println("ERROR!!!!!!!!!!!!!!!!!");
					break;
				case UNHANDLED:
					// Probably connection error
					((TextView) loginErrorTextView).setText(vv.getResources().getString(R.string.login_error_connection));
					loginErrorTextView.setVisibility(View.VISIBLE);
					break;
				case WRONG_INFORMATION:
//					runOnUiThread(new Runnable() {
//						@Override
//						public void run() {
							((TextView) loginErrorTextView).setText(vv.getResources().getString(R.string.login_error_incorrect));
							loginErrorTextView.setVisibility(View.VISIBLE);
//						}
//					});
					break;
				case NOT_ACTIVATED:
					// Send user to activation view
					ActivateUserActivity.setLoginInformation(((EditText)findViewById(R.id.edit_username)).getText().toString(), ((EditText) findViewById(R.id.edit_password)).getText().toString());
					startActivity(new Intent(dis, ActivateUserActivity.class));
				default:
					break;
				}
//			}
//		}.start();
	}
}
