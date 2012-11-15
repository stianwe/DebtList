package requests.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A object that supports serializing to and from a simple XML format
 * 
 * @author Stian Weie
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
abstract public class XMLSerializable implements XMLConstants {
	
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
	protected abstract long getId();
	
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
	 * Serialize this object to XML
	 * 
	 * @return
	 */
	public String toXML() {
		return "<"+TAG_OUTER+">"+toXML(null)+"</"+TAG_OUTER+">";
	}
	
	/**
	 * Serialize this object to XML
	 * 
	 * @param registeredIds id numbers of objects already in the XML
	 * @return XML representation of this object and all referenced objects
	 */
	private String toXML(Set<String> registeredIds) {
		StringBuilder inner = new StringBuilder();
		StringBuilder pre = new StringBuilder();

		if(registeredIds == null) 
			registeredIds = new HashSet<String>();
		registeredIds.add(getGlobalId());
		
		toXML(getVariableMap(), registeredIds, inner, pre, false);
		
		return String.format("%s<object class=\"%s\" id=\"%s\">%s</object>",
				pre, getClass().getName(), getGlobalId(), inner);
	}
	
	/**
	 * Serialize the given Map to XML
	 * 
	 * @param map			map to be serialized
	 * @param registeredIds IDs of already handled objects 
	 * @param inner			builder used to build this object
	 * @param pre			builder used to build XML of references objects
	 */
	private void toXML(Map<String, Object> map, Set<String> registeredIds,
			StringBuilder inner, StringBuilder pre) {
		toXML(map, registeredIds, inner, pre, true);
	}
	
	/**
	 * Serialize the given Map to XML
	 * 
	 * @param map			map to be serialized
	 * @param registeredIds IDs of already handled objects 
	 * @param inner			builder used to build this object
	 * @param pre			builder used to build XML of references objects
	 * @param decorate		if false the outer <map> tags will not be added
	 */
	private void toXML(Map<String, Object> map, Set<String> registeredIds,
			StringBuilder inner, StringBuilder pre, boolean decorate) {
		inner.append("<map>");
		for(String s : map.keySet()) {
			inner.append(String.format("<element key=\"%s\">", s));
			toXML(map.get(s), registeredIds, inner, pre);
			inner.append("</element>");
		}
		inner.append("</map>");
	}
	
	/**
	 * Serialize the given List to XML
	 * 
	 * @param list			list to be serialized
	 * @param registeredIds IDs of already handled objects 
	 * @param inner			builder used to build this object
	 * @param pre			builder used to build XML of references objects
	 */
	private void toXML(List<Object> list, Set<String> registeredIds,
			StringBuilder inner, StringBuilder pre) {
		inner.append("<list>");
		for(Object o : list) {
			inner.append("<element>");
			toXML(o, registeredIds, inner, pre);
			inner.append("</element>");
		}
		inner.append("</list>");
	}
	
	/**
	 * Serialize the given Object to XML
	 * @param obj
	 * @param registeredIds
	 * @param innner
	 * @param pre
	 */
	@SuppressWarnings("unchecked")
	private void toXML(Object obj, Set<String> registeredIds,
			StringBuilder inner, StringBuilder pre) {
		if(obj instanceof List) {
			toXML((List) obj, registeredIds, inner, pre);
		} else if(obj instanceof Map) {
			toXML((Map) obj, registeredIds, inner, pre);
		} else if(obj instanceof String) {
			inner.append(String.format("<%s><![CDATA[%s]]></%s>",
					TAG_STRING, obj.toString(), TAG_STRING));
		} else if(obj instanceof Integer) {
			inner.append("<"+TAG_INTEGER+">"+obj.toString()+"</"+TAG_INTEGER+">");
		} else if(obj instanceof XMLSerializable) {
			XMLSerializable s = (XMLSerializable) obj;
			inner.append(String.format(
					"<%s>%s</%s>", TAG_OBJREF, s.getGlobalId(), TAG_OBJREF
				));
			if(!registeredIds.contains(s.getGlobalId())) {
				pre.append(s.toXML(registeredIds));
			}
		} else if(obj instanceof Double) {
			inner.append("<"+TAG_DOUBLE+">"+obj.toString()+"</"+TAG_DOUBLE+">");
		} else if(obj instanceof Long) {
			inner.append("<"+TAG_LONG+">"+obj.toString()+"</"+TAG_LONG+">");
		} else if(obj instanceof Boolean) {
			inner.append("<"+TAG_BOOLEAN+">"+(obj.toString())+"</"+TAG_BOOLEAN+">");
		} else if(obj instanceof Enum) {
			inner.append("<"+TAG_ENUM+" class=\""+obj.getClass().getName()+"\">"+obj+"</"+TAG_ENUM+">");
		} else if(obj == null) {
			inner.append("<"+TAG_NULL+" />");
		} else {
			throw new RuntimeException("Uknown type "+obj.getClass().getName());
		}
	}
	
	public static void main(String[] args) {
		XMLSerializable o = new XMLSerializable(){			
			@Override
			protected long getId() {
				return 137;
			}
			
		};
		XMLSerializable o1 = new XMLSerializable() {
			protected long getId() { return 7; }
		};
		
		ArrayList<String> l = new ArrayList<String>();
		l.add("This");
		l.add("Is");
		l.add("A list");
		o.setVariable("list", l);
		o.setVariable("s", "This is a string");
		o.setVariable("i", 135486);
		o.setVariable("l", 21354L);
		o.setVariable("b", true);
		o.setVariable("null", null);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("s", "map-in-map");
		m.put("list-in-list", l);
		o.setVariable("m", m);
		o.setVariable("obj", o1);
		
		o1.setVariable("other", o);
		o1.setVariable("test", "123");
		
		System.out.println(o.toXML());
		//XMLDeserializer.toObject(o.toXML());
	}
	
	/*
	 * <xml>
	 * 	<object class="requests.xml.XMLSerializable$2" id="requests.xml.XMLSerializable$2-7">
	 * 			<element key="other"><object-reference>requests.xml.XMLSerializable$1-137</object-reference></element>
	 * 			<element key="test"><string><![CDATA[123]]></string></element>
	 * 	</object>
	 * 	<object class="requests.xml.XMLSerializable$1" id="requests.xml.XMLSerializable$1-137">
	 * 			<element key="b"><boolean>true</boolean></element>
	 * 			<element key="s"><string><![CDATA[This is a string]]></string></element>
	 * 			<element key="obj"><object-reference>requests.xml.XMLSerializable$2-7</object-reference></element>
	 * 			<element key="l"><long>21354</long></element>
	 * 			<element key="list">
	 * 				<list>
	 * 					<element><string><![CDATA[This]]></string></element>
	 * 					<element><string><![CDATA[Is]]></string></element>
	 * 					<element><string><![CDATA[A list]]></string></element>
	 * 				</list>
	 * 			</element>
	 * 			<element key="m">
	 * 				<map>
	 * 					<element key="s"><string><![CDATA[map-in-map]]></string></element>
	 * 					<element key="list-in-list">
	 * 						<list>
	 * 							<element><string><![CDATA[This]]></string></element>
	 * 							<element><string><![CDATA[Is]]></string></element>
	 * 							<element><string><![CDATA[A list]]></string></element>
	 * 						</list>
	 * 					</element>
	 * 				</map>
	 * 			</element>
	 * 			<element key="null"><null /></element>
	 * 			<element key="i"><int>135486</int></element>
	 * 	</object>
	 * </xml>
	 */
	
	/**
	 * The SAX handler object
	 */
	private static XMLObjectHandler handler = new XMLObjectHandler();	
	
	/**
	 * Convert the given XML back into object form
	 * 
	 * @param xml
	 * @return the restored object
	 * @throws IOException on errors parsing the XML
	 */
	public static XMLSerializable toObject(String xml) throws IOException {
		XMLReader xr;
		try {
			xr = XMLReaderFactory.createXMLReader();
			
			// Only a single thread may access the handler at the time
			synchronized(handler) {
		        xr.setContentHandler(handler);
		        xr.setErrorHandler(handler);
		        InputStream xmlStream = new ByteArrayInputStream(
		        		xml.getBytes("UTF-8"));
		        
		        // Parse the data twice
		        xr.parse(new InputSource(xmlStream));
		        xmlStream.reset();
		        xr.parse(new InputSource(xmlStream));
			}
	        return handler.getResultObject();
		} catch (SAXException e) {
			throw new IOException("Invalid XML data", e);
		} catch (IOException e) {
			// Should really never happen (?)
			throw new IOException("IOException handling XML data", e);
		}
	}
}

/**
 * SAX handler used for object restoration
 * 
 * Restoration is a two stage process:
 * 
 * 	1. All objects are recreated, an all simple data types are restored.
 *     Any references to other objects within the same transmission are set
 *     to null.
 *  2. All object references are now restored, as we now have all the
 *     objects in memory
 *     
 *  This means that to get a object from a XML string, one must create a SAX
 *  parser, give it an instance of this class as the handler, and pass it the
 *  XML twice. After the first run it will have all objects reconstructed, but
 *  without any inter-object references. After the second pass all references
 *  between objects will also be in place.
 * 
 * 
 * @TODO The two stage process can be reduces to a one step process. Instead of
 * creating the objects one could create Maps of all variables placing
 * placeholders where needed, and then traverse the entire map including any lists
 * replacing the placeholders at document end. 
 * @TODO Support for Map types?
 */
class XMLObjectHandlerOld extends DefaultHandler implements XMLConstants  {
	
	private Map<String, XMLSerializable> reg;
	private XMLSerializable object;
	private String varName;
	private String type;
	private List<Object> list;
	private int stage = -1;
	private int listPos;
	private Class<? extends Enum<?>> enumClass;
	
	private XMLSerializable resultObject;
	
	/**
	 * Start of a tag
	 * 
	 * Depending on the current state store the name of the tag in a
	 * variable for later reference
	 * 
	 * @param uri
	 * @param name
	 * @param qName
	 * @param attributes
	 * @throws SAXException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void startElement(String uri, String name, String qName,
			Attributes attributes) throws SAXException {

		if(name.equals(TAG_OUTER)) {
			if(stage == -1 || stage == 2) {
				stage = 1;
				reg = new HashMap<String, XMLSerializable>();
				varName = type = null; list = null; object = null; enumClass = null;
			} else if(stage == 1) {
				stage = 2;
			}
		} else if(object == null) {
			if(stage == 1) {
				try {
					Object o = getClass().getClassLoader().loadClass(name)
							.newInstance();
					if(o instanceof XMLSerializable) {
						object = (XMLSerializable) o;
						reg.put(attributes.getValue("id"), object);
					} else {
						throw new Exception("Class "+name+" is not an"+
								"instance of XMLSerializable");
					}
				} catch (Exception e) {
					throw new SAXException(
							"Unable to instanciate sent class "+name, e
							);
				}
			} else if(stage == 2) {
				object = reg.get(attributes.getValue("id"));
			}
		} else if(varName == null) {
			varName = name;
		} else if(type == null) {
			if(name.equals(TAG_LIST)) {
				listPos = 0;
				if(stage == 1) {
					list = new LinkedList<Object>();						
					object.setVariable(varName, list);
				} else if(stage == 2) {
					list = (List<Object>) object.getVariable(varName);
				}
			} else if(name.equals(TAG_ENUM)) {
				try {
					enumClass = (Class<? extends Enum<?>>) getClass()
							.getClassLoader().loadClass(attributes.getValue("class"));
					type = TAG_ENUM;
				} catch (ClassNotFoundException e) {
					throw new SAXException(
							"Unable to instanciate sent enum class "+name, e
							);
				}
			} else if(name.equals(TAG_NULL)) {
				object.setVariable(varName, null);
				varName = null;
			} else {
				type = name;
			}
		}				
	}
	
	/**
	 * End of a tag
	 * 
	 * 
	 * @param uri
	 * @param localName
	 * @param qName
	 * @throws SAXException
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(list != null && localName.equals(TAG_LIST)) {
			list = null;
			varName = null;
		} else if(object != null && localName.equals(object.getClass().getName())) {
			resultObject = (XMLSerializable) object;
			object = null;				
		}
	};
	
	/**
	 * Raw data found 
	 * 
	 * If this should be the value of a parameter we'll store it where it
	 * belongs
	 * 
	 * @param ch
	 * @param start
	 * @param length
	 * @throws SAXException
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(varName != null && type != null) {				
			if(stage == 1) {
				Object obj = toObjectHelper(new String(ch, start, length));
				if(list != null) {
					list.add(obj);
				} else {
					object.setVariable(varName, obj);
					varName = null;
				}
				enumClass = null; type = null;
			} else if(stage == 2) {			
				if(type.equals(TAG_OBJREF)) {
					Object obj = reg.get(new String(ch, start, length));
					if(list != null) {
						list.remove(listPos);
						list.add(listPos, obj);
						type = null;
					} else {
						object.setVariable(varName, obj);
						varName = type = null;
					}
				}
				if(list != null) {
					type = null;
					listPos++;
				} else {
					varName = type = null;
				}
			}
		}
	}
	
	/**
	 * Convert a data string to the current type
	 * 
	 * @param data
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object toObjectHelper(String data) {
		if(type.equals(TAG_INTEGER)) {
			return Integer.parseInt(data);
		} else if(type.equals(TAG_STRING)) {
			return data;
		} else if(type.equals(TAG_OBJREF)){
			// Placeholder data
			return null;				
		} else if(type.equals(TAG_BOOLEAN)) {
			return data.equals("true");
		} else if(type.equals(TAG_LONG)) {
			return Long.parseLong(data);
		} else if(type.equals(TAG_DOUBLE)) {
			return Double.parseDouble(data);
		} else if(type.equals(TAG_ENUM)) {
			return Enum.valueOf((Class<? extends Enum>) enumClass, data);
		} else {
			System.err.println("Unknown type "+type);
		}
		return null;
	}	
	
	/**
	 * Returns the fully reconstructed result object
	 * 
	 * @return
	 */
	public XMLSerializable getResultObject() {
		return resultObject;
	}
};