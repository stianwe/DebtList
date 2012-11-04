package logic;

import requests.XMLParsable;

public abstract class Sendable extends XMLParsable {

	/**
	 * Returns a sendable version of this object (avoiding infinite loops for example).
	 * @param fromServer	If the returned object will be sent from the server (true) or not (false). 
	 * @return				This object as a sendable version
	 */
	public abstract Sendable toSendable(boolean fromServer);
}
