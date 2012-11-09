package xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
		return "<xml>"+toXML(null)+"</xml>";
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
		return pre.toString() + "<" + getClass().getName() + " id=\""+getGlobalId()+"\">\n" +
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
	
	/**
	 * After the SAX handlers two stage process this object is set to the
	 * resulting object. (The resulting object is always the last object
	 * defined within the XML)
	 */
	private static Object toObject;
	
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
	 */
	private static DefaultHandler handler = new DefaultHandler() {
		
		private Map<String, XMLSerializable> reg;
		private XMLSerializable object;
		private String varName;
		private String type;
		private List list;
		private int stage = -1;
		private int listPos;
		
		/**
		 * Start of a tag
		 * 
		 * Depending on the current state store the name of the tag in a
		 * variable for later reference
		 * 
		 * @param uri
		 * @param localName
		 * @param qName
		 * @param attributes
		 * @throws SAXException
		 */
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			if(localName.equals("xml")) {
				if(stage == -1 || stage == 2) {
					stage = 1;
					reg = new HashMap<String, XMLSerializable>();
					varName = type = null; list = null; object = null;
				} else if(stage == 1) {
					stage = 2;
				}
			} else if(object == null) {
				if(stage == 1) {
					try {
						Object o = getClass().getClassLoader().loadClass(localName)
								.newInstance();
						if(o instanceof XMLSerializable) {
							object = (XMLSerializable) o;
							reg.put(attributes.getValue("id"), object);
						} else {
							throw new RuntimeException("Unknown class name "+localName);
						}
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if(stage == 2) {
					object = reg.get(attributes.getValue("id"));
				}
			} else if(varName == null) {
				varName = localName;
			} else if(type == null) {
				if(localName.equals("list")) {
					listPos = -1;
					if(stage == 1) {
						list = new LinkedList();						
						object.setVariable(varName, list);
					} else if(stage == 2) {
						list = (List) object.getVariable(varName);
					}
				} else {
					type = localName;
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
			if(list != null && localName.equals("list")) {
				list = null;
				varName = null;
			} else if(object != null && localName.equals(object.getClass().getName())) {
				if(stage == 2) toObject = object;
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
						type = null;
					} else {
						object.setVariable(varName, obj);
						varName = type = null;
					}
				} else if(stage == 2) {
					if(list != null) listPos++;					
					if(type.equals("xmlserializable")) {
						Object obj = reg.get(new String(ch, start, length));
						if(list != null) {
							list.remove(listPos);
							list.add(obj);
							type = null;
						} else {
							object.setVariable(varName, obj);
							varName = type = null;
						}
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
		private Object toObjectHelper(String data) {
			if(type.equals("int")) {
				return Integer.parseInt(data);
			} else if(type.equals("string")) {
				return data;
			} else if(type.equals("xmlserializable")){
				// Placeholder data
				return null;				
			} else {
				System.err.println("Unknown type "+type);
			}
			return null;
		}		
	};
	
	/**
	 * Convert the given XML back into object form
	 * 
	 * This is a two stage process, implemented in the DefaultHandler instance
	 * above
	 * 
	 * @param xml
	 */
	public static Object toObject(String xml) {
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
	        
	        return toObject;	        
	        
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		User u = new User();
		System.out.println(u.toXML());
		User u1 = (User) XMLSerializable.toObject(u.toXML());
		System.out.println(u1.getVariable("group"));
		
		
	}
	
}
