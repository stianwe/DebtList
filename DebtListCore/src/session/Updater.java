package session;

import java.util.Timer;

public class Updater {

	protected UpdateRequester updateRequester;
	protected Timer timer;
	
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
}
