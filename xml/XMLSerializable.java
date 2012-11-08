package xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A object that supports serializing to and from a simple XML format
 * 
 * @author Stian Weie
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
abstract public class XMLSerializable {

	/**
	 * All serializable variables in the object
	 */
	private Map<String, Object> variables = null;
	
	/**
	 * Get a map of all variables
	 * 
	 * This will on-demand construct the variable map of the XMLSerializable
	 * object, and should be used when ever one need access to the map
	 * 
	 * @return
	 */
	private Map<String, Object> getVariableMap() {
		if(variables == null) {
			variables = new HashMap<String, Object>();
		}
		return variables;
	}
	
	/**
	 * Get a id number that uniquely identifies this XMLSerializable object
	 * from all other object of the same class
	 * 
	 * @return
	 */
	protected abstract String getId();
	
	/**
	 * Get a globally unique id number by combining the Id with the class name
	 * 
	 * @return
	 */
	private String getGlobalId() {
		return String.format("%s-%s", getClass().getName(), getId());
	}
	
	/**
	 * Get the value of the given key or null if the key does not exist
	 * 
	 * @param key
	 * @return value if variable _key_ or null if _key_ is unknown
	 */
	protected Object getVariable(String key) {
		return getVariableMap().get(key);
	}
	
	/**
	 * Set the value of the given variable
	 * 
	 * @param key
	 * @param value
	 */
	protected void setVariable(String key, Object value) {
		getVariableMap().put(key, value);
	}
	
	/**
	 * Serialize the variables in the variable map to XML
	 * 
	 * @return
	 */
	public String toXML() {
		return toXML(null);
	}
	
	/**
	 * Serialize the variables in the variable map to XML
	 * 
	 * @param registeredIds id numbers of objects already in the XML
	 * @return
	 */
	private String toXML(Set<String> registeredIds) {
		StringBuilder inner = new StringBuilder();
		StringBuilder pre = new StringBuilder();

		if(registeredIds == null) 
			registeredIds = new HashSet<String>();
		registeredIds.add(getGlobalId());
		for(String key : getVariableMap().keySet()) {
			inner.append("<"+key+">");
			serializeValue(registeredIds, inner, pre, getVariable(key));
			inner.append("</"+key+">");
		}
		return pre.toString() + "<" + getClass().getName() + ">\n" +
			"\n" + inner.toString() + "</" + getClass().getName() + ">";
	}
	
	/**
	 * Serialize a single value to a XML representation
	 * 
	 * @param registeredIds
	 * @param inner
	 * @param obj
	 */
	@SuppressWarnings("rawtypes")
	private void serializeValue(Set<String> registeredIds, StringBuilder inner, StringBuilder pre, Object obj) {
		if(obj instanceof String) {
			inner.append("<string>"+obj.toString()+"</string>");					
		} else if(obj instanceof Integer) {
			inner.append("<int>"+obj.toString()+"</int>");
		} else if(obj instanceof XMLSerializable) {
			XMLSerializable s = (XMLSerializable) obj;
			inner.append(String.format(
					"<xmlserializable>%s</xmlserializable>", 
					s.getGlobalId()
				));
			if(!registeredIds.contains(s.getGlobalId())) {
				pre.append(s.toXML(registeredIds));
			}
		} else if(obj instanceof List) {
			List l = (List) obj;
			inner.append("<list>");
			for(Object lo : l) {
				serializeValue(registeredIds, inner, pre, lo);
			}
			inner.append("</list>");
		} else {
			throw new RuntimeException("Uknown type "+obj.getClass().getName());
		}
	}
	
	public static void main(String[] args) {
		XMLSerializable g = new XMLSerializable(){
			protected String getId() {
				return "group-1";
			}
		};
		XMLSerializable o = new XMLSerializable() {			
			@Override
			protected String getId() {
				return "1";
			}
		};
		List<XMLSerializable> list = new LinkedList<XMLSerializable>();
		list.add(o);
		g.setVariable("users", list);
		o.setVariable("id", 1);
		o.setVariable("username", "runar");
		o.setVariable("group", g);
		
		System.out.println(o.toXML());
		
	}
	
}
