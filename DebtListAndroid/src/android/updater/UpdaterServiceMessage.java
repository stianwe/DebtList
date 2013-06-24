package android.updater;

import requests.xml.XMLSerializable;

public class UpdaterServiceMessage extends XMLSerializable {

	private static final String SHOULD_UPDATE_WITHOUT_WIFI_KEY = "shouldUpdateWithoutWifi";
	
	public UpdaterServiceMessage(boolean shouldUpdateWithoutWifi) {
		setShouldUpdateWithoutWifi(shouldUpdateWithoutWifi);
	}
	
	public void setShouldUpdateWithoutWifi(boolean b) {
		setVariable(SHOULD_UPDATE_WITHOUT_WIFI_KEY, b);
	}
	
	public boolean shouldUpdateWithoutWifi() {
		return (Boolean) getVariable(SHOULD_UPDATE_WITHOUT_WIFI_KEY);
	}
	
	@Override
	protected long getId() {
		return 0;
	}

}
