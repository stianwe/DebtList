package android.utils;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.debtlistandroid.R;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public abstract class Tools {

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
		mNotificationManager.notify(0, mBuilder.build());
	}
}
