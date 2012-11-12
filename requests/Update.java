package requests;

import java.util.ArrayList;
import java.util.List;

import requests.xml.XMLSerializable;

public class Update extends Request {

	public Update() {
		setVariable("objects", new ArrayList<XMLSerializable>());
	}
	
	private List<XMLSerializable> getObjects() {
		return (List) getVariable("objects");
	}
	
	public synchronized void add(XMLSerializable o) {
		getObjects().add(o);
	}
	
	public synchronized XMLSerializable get(int i) {
		return getObjects().get(i);
	}
	
	public synchronized int size() {
		return getObjects().size();
	}
}
