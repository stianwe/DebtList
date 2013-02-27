package session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import requests.UpdateRequest;
import requests.xml.XMLSerializable;

/**
 * A helper class that will poll the server connected to in PCSession.session for updates regarding the user
 * specified in PCSession.session. 
 */
public class UpdateRequester extends TimerTask {

	protected long timeBetweenUpdates, lastUpdate = 0;
	
	/**
	 * Initializes a new UpdateRequester
	 * @param timeBetweenUpdates	Specify the time (in ms) between update requests
	 */
	public UpdateRequester(long timeBetweenUpdates) {
		this.timeBetweenUpdates = timeBetweenUpdates;
		updateLastUpdate();
	}
	
	public void updateLastUpdate() {
		lastUpdate = System.currentTimeMillis();
	}
	
	@Override
	public void run() {
		// Only run updates after the given time interval (mainly so that we will not poll for updates
		// while the user is sending/receiving something).
//		if(System.currentTimeMillis() - lastUpdate >= timeBetweenUpdates) {
			update();
			updateLastUpdate();
//		}
	}
	
	/**
	 * Poll the server connected to in Session.session for updates regarding the user
	 * specified in Session.session. 
	 */
	public static UpdateRequest fetchUpdates() throws IOException{
		return (UpdateRequest) XMLSerializable.toObject(Session.session.sendAndReceive(new UpdateRequest().toXML()));
	}

	/**
	 * Process all updates
	 * @return	The processed updates
	 */
	public List<XMLSerializable> update() {
		List<XMLSerializable> l = new ArrayList<XMLSerializable>();
		System.out.println("Updating..");
		try {
			UpdateRequest response = fetchUpdates();
			for (int i = 0; i < response.size(); i++) {
				System.out.println("Processing update..");
				l.add(response.get(i));
				Session.session.processUpdate(response.get(i));
			}
			System.out.println("Done updating.");
		} catch (IOException e) {
			System.out.println("Automatic update failed.");
			// TODO: What else?
		}
		return l;
	}
}

