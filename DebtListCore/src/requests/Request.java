package requests;

import requests.xml.XMLSerializable;
import versioning.Version;

public abstract class Request extends XMLSerializable {

	/**
	 * Returns 1, which is sufficient for a Request 
	 */
	@Override
	protected long getId() {
		return 1;
	}

//	public Version getClientVersion() {
//		return (Version) getVariable("client_version");
//	}
//	
//	public void setClientVersion(Version v) {
//		setVariable("client_version", v);
//	}
	
	public Version getServerVersion() {
		return Version.parseVersion((String) getVariable("server_version"));
	}
	
	public void setServerVersion(Version v) {
		setVariable("server_version", v.toString());
	}
}
