package test.requests.xml;

import requests.xml.XMLSerializable;

public class TestModel extends XMLSerializable {

	public long getId() {
		return 1;
	}
	
	public void setVariable(String key, Object value) {
		super.setVariable(key,  value);
	}
	
	public Object getVariable(String key) {
		return super.getVariable(key);
	}
	
}
