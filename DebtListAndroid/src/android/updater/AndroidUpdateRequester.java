package android.updater;

import java.util.List;

import logic.Debt;
import logic.DebtStatus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.debtlistandroid.DebtViewActivity;
import android.debtlistandroid.FriendViewActivity;
import android.net.ConnectivityManager;
import android.utils.Tools;

import requests.FriendRequest;
import requests.FriendRequest.FriendRequestStatus;
import requests.xml.XMLSerializable;
import session.Session;
import session.UpdateRequester;

/**
 * @deprecated No longer in use, since normal threads in the background get killed almost instantly. See UpdaterService instead!
 * @author Stian
 *
 */
public class AndroidUpdateRequester extends UpdateRequester {

	private Context context;
	private boolean shouldUpdateWithoutWifi;
	
	public AndroidUpdateRequester(long timeBetweenUpdates, boolean shouldUpdateWithoutWifi, Context context) {
		super(timeBetweenUpdates);
		this.context = context;
		this.shouldUpdateWithoutWifi = shouldUpdateWithoutWifi;
	}

	@Override
	public void run() {
		// Only run updates after the given time interval (mainly so that we will not poll for updates
		// while the user is sending/receiving something).
		//				if(System.currentTimeMillis() - lastUpdate >= timeBetweenUpdates) {
		
		// Display dummy notification
//		Tools.createNotification(context, "UPDATE", "DebtList has updated", DebtViewActivity.class, DebtViewActivity.class);
		
		// Don't update without wifi, if user has specified it
		if(!shouldUpdateWithoutWifi && 
			!((ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()){
			System.out.println("Won't update since we are not connected to wifi!");
			return;
		} else {
			System.out.println("Updating..!");
		}
			
		List<XMLSerializable> updates = update();
		System.out.println("ANDROID AHAHHAAHAHAHHAHAASJDKLAJSDKLJAKSDJ RUNAR!");
		for (XMLSerializable u : updates) {
			System.out.println("DISPLAY NOTIFICATION!!!!!!");
			System.out.println(u);
			createNotification(u);
		}
		super.updateLastUpdate();
		//				}
	}

	/**
	 * Helper method to create notification for an update
	 * @param update
	 */
	private void createNotification(XMLSerializable update) {
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
