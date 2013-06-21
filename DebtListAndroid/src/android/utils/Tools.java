package android.utils;

import network.Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.debtlistandroid.DebtViewActivity;
import android.debtlistandroid.R;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public abstract class Tools {

	private static int notificationCounter = 0;

	/**
	 * Create and display an Android notification
	 * 
	 * @param context
	 * @param title
	 * @param text
	 * @param parent
	 * @param source
	 */
	public static void createNotification(Context context, String title, String text, Class<? extends Activity> parent, Class<? extends Activity> source) {
		System.out.println("Displaying notification!");
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
		.setContentTitle(title)
		.setContentText(text).setSmallIcon(R.drawable.ic_launcher);
		Intent resultIntent = new Intent(context, source);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(parent);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
				stackBuilder.getPendingIntent(
						0,
						PendingIntent.FLAG_UPDATE_CURRENT
						);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(notificationCounter++, mBuilder.build());
	}

	/**
	 * Create and display a debug notification if debug flag (in DebtListCore.network.Constants) is set.
	 * 
	 * @param context
	 * @param title
	 * @param text
	 * @param parent
	 * @param source
	 */
	public static void createDebugNotification(Context context, String title, String text) {
		if(Constants.ANDROID_DEBUG_MODE) {
			createNotification(context, "[DEBUG] " + title, text, DebtViewActivity.class, DebtViewActivity.class);
		}
	}

	public static void displayIncompatibleVersionDialog(Context context) {
		Tools.displayDialog("Incompatible version!", "You are using an outdated version of this application, that is no longer compatible with the current one. Please visit http://github.com/stianwe/DebtList to update!", "OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Tools.lastCreatedDialog.cancel();
			}
		}, null, null, context);
	}
	
	public static void displayDialog(String title, String message, String yesOrOkText, DialogInterface.OnClickListener yesOrOkListener, String noText, DialogInterface.OnClickListener noListener, Context context) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

		// set title
		alertDialogBuilder.setTitle(title);

		// set dialog message
		alertDialogBuilder.setMessage(message);
		if(noListener != null) {
			alertDialogBuilder.setNegativeButton(noText, noListener);
		}
		if(yesOrOkListener == null) {
			alertDialogBuilder.setCancelable(true);
		} else {
			alertDialogBuilder.setCancelable(false);
			alertDialogBuilder.setPositiveButton(yesOrOkText ,yesOrOkListener);
		}
		
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		lastCreatedDialog = alertDialog;
		
		// show it
		alertDialog.show();
	}
	
	public static Dialog lastCreatedDialog;
}
