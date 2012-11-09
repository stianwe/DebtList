package requests.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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
abstract public class XMLSerializable {

	private static final String TAG_LIST = "list";
	private static final String TAG_STRING = "string";
	private static final String TAG_INTEGER = "int";
	private static final String TAG_DOUBLE = "double";
	private static final String TAG_BOOLEAN = "boolean";
	private static final String TAG_LONG = "long";
	private static final String TAG_XMLSER = "xmlserializable";
	private static final String TAG_ENUM = "enum";
	private static final String TAG_OUTER = "xml";
	
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
	 * Serialize the variables in the variable map to XML
	 * 
	 * @return
	 */
	public String toXML() {
		return "<"+TAG_OUTER+">"+toXML(null)+"</"+TAG_OUTER+">";
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
	private void serializeValue(Set<String> registeredIds, StringBuilder inner, StringBuilder pre, Object obj) {
		if(obj instanceof String) {
			inner.append("<"+TAG_STRING+"><![CDATA["+obj.toString()+"]]></"+TAG_STRING+">");					
		} else if(obj instanceof Integer) {
			inner.append("<"+TAG_INTEGER+">"+obj.toString()+"</"+TAG_INTEGER+">");
		} else if(obj instanceof XMLSerializable) {
			XMLSerializable s = (XMLSerializable) obj;
			inner.append(String.format(
					"<%s>%s</%s>", TAG_XMLSER, s.getGlobalId(), TAG_XMLSER
				));
			if(!registeredIds.contains(s.getGlobalId())) {
				pre.append(s.toXML(registeredIds));
			}
		} else if(obj instanceof List<?>) {
			List<?> l = (List<?>) obj;
			inner.append("<"+TAG_LIST+">");
			for(Object lo : l) {
				serializeValue(registeredIds, inner, pre, lo);
			}
			inner.append("</"+TAG_LIST+">");
		} else if(obj instanceof Double) {
			inner.append("<"+TAG_DOUBLE+">"+obj.toString()+"</"+TAG_DOUBLE+">");
		} else if(obj instanceof Long) {
			inner.append("<"+TAG_LONG+">"+obj.toString()+"</"+TAG_LONG+">");
		} else if(obj instanceof Boolean) {
			inner.append("<"+TAG_BOOLEAN+">"+(obj.toString())+"</"+TAG_BOOLEAN+">");
		} else if(obj instanceof Enum) {
			inner.append("<"+TAG_ENUM+" class=\""+obj.getClass().getName()+"\">"+obj+"</"+TAG_ENUM+">");
		} else {
			throw new RuntimeException("Uknown type "+obj.getClass().getName());
		}
	}
	
	/**
	 * After the SAX handlers two stage process this object is set to the
	 * resulting object. (The resulting object is always the last object
	 * defined within the XML)
	 */
	private static XMLSerializable toObject;
	
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
	@SuppressWarnings("rawtypes")
	private static DefaultHandler handler = new DefaultHandler() {
		
		private Map<String, XMLSerializable> reg;
		private XMLSerializable object;
		private String varName;
		private String type;
		private List<Object> list;
		private int stage = -1;
		private int listPos;
		private Class enumClass;
		
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
					varName = type = null; list = null; object = null;
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
						enumClass = getClass().getClassLoader()
								.loadClass(attributes.getValue("class"));
					} catch (ClassNotFoundException e) {
						throw new SAXException(
								"Unable to instanciate sent enum class "+name, e
								);
					}
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
				toObject = (XMLSerializable) object;
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
		@SuppressWarnings("unchecked")
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if(varName != null && type != null) {				
				if(stage == 1) {
					Object obj = toObjectHelper(new String(ch, start, length));
					if(list != null) {
						list.add(obj);
						type = null;
					} else if(enumClass != null) { 
						object.setVariable(varName, Enum.valueOf(enumClass, 
								new String(ch, start, length)));
					} else {
						object.setVariable(varName, obj);
						varName = type = null;
					}
				} else if(stage == 2) {			
					if(type.equals(TAG_XMLSER)) {
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
		private Object toObjectHelper(String data) {
			if(type.equals(TAG_INTEGER)) {
				return Integer.parseInt(data);
			} else if(type.equals(TAG_STRING)) {
				return data;
			} else if(type.equals(TAG_XMLSER)){
				// Placeholder data
				return null;				
			} else if(type.equals(TAG_BOOLEAN)) {
				return data.equals("true");
			} else if(type.equals(TAG_LONG)) {
				return Long.parseLong(data);
			} else if(type.equals(TAG_DOUBLE)) {
				return Double.parseDouble(data);
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
	 * @return the restored object
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
	        
	        return toObject;	        
	        
		} catch (SAXException e) {
			throw new IOException("Invalid XML data", e);
		} catch (IOException e) {
			// Should really never happen
			throw new IOException("IOException handling XML data", e);
		}
	}
}