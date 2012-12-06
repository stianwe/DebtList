package session;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import requests.UpdateRequest;
import requests.xml.XMLSerializable;

public class Updater {

	private UpdateRequester updateRequester;
	private Timer timer;
	
	public void startUpdater(long timeBetweenUpdates) {
		updateRequester = new UpdateRequester(timeBetweenUpdates);
		(timer = new Timer()).schedule(updateRequester, timeBetweenUpdates, timeBetweenUpdates);
	}
	
	/**
	 * @return	The time in millisec to the next scheduled update
	 */
	public long getTimeToUpdate() {
		return updateRequester.lastUpdate + updateRequester.timeBetweenUpdates - System.currentTimeMillis();
	}
	
	/**
	 * Stops the scheduled updater.
	 * @return True if this call stops the updater from updating anymore. False if it was already stopped.
	 */
	public boolean stopUpdater() {
//		if(updateRequester == null) return false;
//		return updateRequester.cancel();
		if(timer == null) return false;
		timer.cancel();
		return true;
	}
	
	/**
	 * Force an update
	 */
	public void update() {
		updateRequester.update();
	}
	
	/**
	 * A helper class that will poll the server connected to in Session.session for updates regarding the user
	 * specified in Session.session. 
	 */
	class UpdateRequester extends TimerTask {

		protected long timeBetweenUpdates, lastUpdate = 0;
		
		/**
		 * Initializes a new UpdateRequester
		 * @param timeBetweenUpdates	Specify the time (in ms) between update requests
		 */
		public UpdateRequester(long timeBetweenUpdates) {
			this.timeBetweenUpdates = timeBetweenUpdates;
			lastUpdate = System.currentTimeMillis();
		}
		
		@Override
		public void run() {
			// Only run updates after the given time interval (mainly so that we will not poll for updates
			// while the user is sending/receiving something).
//			if(System.currentTimeMillis() - lastUpdate >= timeBetweenUpdates) {
				update();
				lastUpdate = System.currentTimeMillis();
//			}
		}

		/**
		 * Poll the server connected to in Session.session for updates regarding the user
		 * specified in Session.session. 
		 */
		public void update() {
			System.out.println("Updating..");
			Session.session.send(new UpdateRequest().toXML());
			try {
				UpdateRequest response = (UpdateRequest) XMLSerializable.toObject(Session.session.receive());
				for (int i = 0; i < response.size(); i++) {
					Session.session.processUpdate(response.get(i));
				}
				System.out.println("Done updating.");
			} catch (IOException e) {
				System.out.println("Automatic update failed.");
				// TODO: What else?
			}
		}
	}
}
