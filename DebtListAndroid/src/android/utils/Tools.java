package android.utils;

import logic.Debt;
import logic.DebtStatus;
import requests.FriendRequest;
import requests.FriendRequest.FriendRequestStatus;
import requests.xml.XMLSerializable;
import session.Session;
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
import android.debtlistandroid.FriendViewActivity;
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
		Tools.displayDialog("Incompatible version!", 
				"You are using an outdated version of this application, that is no longer compatible with the current one. Please visit " + Constants.NEW_VERSION_URL + " to update!", 
				"OK", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Tools.lastCreatedDialog.cancel();
					}
				}, 
				null, null,
				context);
	}
	
	public static void displayOutdatedVersionDialog(Context context) {
		Tools.displayDialog("Outdated version!", 
				"You are using an outdated version of this application.\nIt is still compatible with the server, but it is strongly adviced to update. Please visit " + Constants.NEW_VERSION_URL + " for more information!", 
				"OK", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Tools.lastCreatedDialog.cancel();
					}
				}, 
				null, null, 
				context);
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
	
	/**
	 * Helper method to create notification for an update
	 * @param update
	 */
	public static void createNotification(XMLSerializable update, Context context) {
		System.out.println("Attempting to create notification for update");
		if(update instanceof FriendRequest) {
			System.out.println("FRIEND REQUEST");
			FriendRequest fr = (FriendRequest) update;
			switch(fr.getStatus()) {
			// Check if it is a new friend request to this user
			case PENDING:
				// Check that it is not this user that created the request
				if(!fr.getFromUser().equals(Session.session.getUser())) {
					Tools.createNotification(context, "New friend request!", fr.getFromUser().getUsername() + " has added you as friend." , FriendViewActivity.class, FriendViewActivity.class);
				} else {
					// DEBUG
					Tools.createDebugNotification(context, "Received self-made update", "Friend request to " + fr.getFriendUsername());
				}
				break;
			// or if it is a friend request that has been accepted/declined
			case ACCEPTED: case DECLINED:
				// TODO Let the user choose if this notification should be displaued?
				Tools.createDebugNotification(context, "Friend request was " + (fr.getStatus() == FriendRequestStatus.ACCEPTED ? "accepted" : "declined"), fr.getFriendUsername());
				break;
			default:
				// We received a friend request that we did not expect..
				// TODO Log or display error message.. Perhaps display error message
				// if debug flag (which does not exist yet) is set
				Tools.createDebugNotification(context, "Invalid friend request!", "Invalid status on received friend request: " + fr.getStatus());
			}
		} else if(update instanceof Debt) {
			System.out.println("DEBT");
			Debt d = (Debt) update;
			String otherUser = (d.getFrom().equals(Session.session.getUser()) ? d.getTo().getUsername() : d.getFrom().getUsername());
			// Check if it is a new debt, or if it is a debt that has been modified/accepted/declined/completed
			String subject = "", text = "";
			switch(d.getStatus()) {
			case REQUESTED:
				System.out.println("REQUESTED DEBT");
				// Check that it is not this user that created the debt
				if(!d.getRequestedBy().equals(Session.session.getUser())) {
					System.out.println("BY OTHER USER");
					subject = "Received new debt from " + otherUser;
				} else {
					// DEBUG
					System.out.println("BY THIS USER");
					Tools.createDebugNotification(context, "Received self-made update", "New debt with id=" + d.getId());
				}
				break;
			case DECLINED: case CONFIRMED:
				// Check that it is not this user that confirmed/declined the debt
				if(!d.getTo().equals(Session.session.getUser())) {
					subject = otherUser + " has " + (d.getStatus() == DebtStatus.DECLINED ? "declined" : "confirmed") + " your debt";
				} else {
					// DEBUG
					Tools.createDebugNotification(context, "Received self-made update", "Accepted/declined debt with id=" + d.getId());
				}
				break;
			case COMPLETED_BY_FROM: case COMPLETED_BY_TO:
				// Check that it is not this user that flagged the debt as completed,
				// to not make the user receive notifications for something
				// he did himself
				if(!((d.getStatus() == DebtStatus.COMPLETED_BY_FROM && d.getFrom().equals(Session.session.getUser())) ||
						d.getStatus() == DebtStatus.COMPLETED_BY_TO && d.getTo().equals(Session.session.getUser()))) {
					subject = otherUser + "has marked a debt as completed";
				} else {
					// DEBUG
					Tools.createDebugNotification(context, "Received self-made update", "Debt has been marked as completed, id=" + d.getId());
				}
				break;
			case COMPLETED:
				// TODO Should check if it is this user that made the final	 complete,
				// but how can we do this?
				subject = "A debt with " + otherUser + " has been completed";
				break;
			default:
				// Nothing to display
				Tools.createDebugNotification(context, "Received hidden update", "Status=" + d.getStatus() + ", id=" + d.getId());
			}
			text = d.getAmount() + " " + d.getWhat() + " " + (d.getTo().equals(d.getRequestedBy()) ? "to " : "from ") + d.getRequestedBy().getUsername();
			if(!subject.equals("")) {
				Tools.createNotification(context, subject, text, DebtViewActivity.class, DebtViewActivity.class);
			}
		} else {
			System.out.println("UNKNOWN!");
			// Received something unknown
			Tools.createDebugNotification(context, "Received something unknown!", update.toString());
		}
	}
}
