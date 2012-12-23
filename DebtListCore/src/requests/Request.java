package requests;

import requests.xml.XMLSerializable;

public abstract class Request extends XMLSerializable {

	/**
	 * Returns 1, which is sufficient for a Request 
	 */
	@Override
	protected long getId() {
		return 1;
	}

}
