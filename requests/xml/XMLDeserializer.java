package requests.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The XMLDesrializer implements the invers functionallity of the toXML method
 * in XMLSerializable, taking a XML document and reconstructing the object
 * graph
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public abstract class XMLDeserializer {
	
	private static XMLReader xr;
	private static XMLObjectHandlerOld handler = new XMLObjectHandlerOld();
	
	/**
	 * Take a XML string and return the XMLSerializable object it represents
	 * @param xml
	 * @return
	 * @throws IOException
	 */
	public static synchronized XMLSerializable toObject(String xml) 
		throws IOException {
		try {
			if(xr == null) {
				xr = XMLReaderFactory.createXMLReader();
				xr.setContentHandler(handler);
		        xr.setErrorHandler(handler);
			}
			
	        InputStream xmlStream = new ByteArrayInputStream(
	        		xml.getBytes("UTF-8"));
	        xr.parse(new InputSource(xmlStream));
	        xmlStream.reset();
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
 */
class XMLObjectHandler extends DefaultHandler implements XMLConstants  {
	
	private static final int STATE_INITIAL          = 0;
	private static final int STATE_AWAITING_OBJ     = 1;
	private static final int STATE_FIRST_MAP        = 2;
	private static final int STATE_AWAITING_ELEMENT = 3;
	private static final int STATE_AWAITING_VALUE   = 4;
	
	/**
	 * A collection, either a map or list
	 */
	class Collection {
		private List<Object> list;
		private Map<String, Object> map;
	}
	
	/**
	 * The resulting object (Always the last object mentioned in the XML)
	 */
	private XMLSerializable resultObject;
	
	/**
	 * Current registry of restored object, indexed by object ID
	 */
	private HashMap<String, XMLSerializable> reg;
	
	private Stack<Collection> colStack;
	
	/**
	 * Current internal state
	 */
	private int state;
	
	/**
	 * Parsing starting; set up intial state
	 */
	@Override
	public void startDocument() throws SAXException {
		reg = new HashMap<String, XMLSerializable>();
		colStack = null;
		resultObject = null;
		state = STATE_INITIAL;
	}
	
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
	@Override
	public void startElement(String uri, String name, String qName,
			Attributes attributes) throws SAXException {

		switch(state) {
		
		// We are waiting for the outer tag
		case STATE_INITIAL:
			if(!name.equals(TAG_OUTER)) {
				throw new SAXException("Expected tag <"+TAG_OUTER+"> got <"+name+">");
			}
			state = STATE_AWAITING_OBJ;
			break;
			
		// We are waiting for a object tag
		case STATE_AWAITING_OBJ:
			if(!name.equals("object")) {
				throw new SAXException("Expected tag <object> got <"+name+">");
			}
			String id = attributes.getValue("id");
			String clazz = attributes.getValue("class");
			if(id == null || clazz == null) {
				throw new SAXException("<object> tag should contain both an id and class name");
			}
			colStack = new Stack<Collection>();		
			Map<String, Object> map = new HashMap<String, Object>();
			
			XMLSerializable object;
			try {
				Object o = getClass().getClassLoader().loadClass(clazz)
						.getConstructor(new Class[]{map.getClass()})
						.newInstance(map);
				if(o instanceof XMLSerializable) {
					object = (XMLSerializable) o;
				} else {
					throw new Exception("Class "+name+" is not an"+
							"instance of XMLSerializable");
				}
			} catch (Exception e) {
				throw new SAXException(
						"Unable to instanciate sent class "+name, e
						);
			}	
			Collection col = new Collection();
			col.map = map;
			colStack.push(col);
			reg.put(id, object);
			state = STATE_AWAITING_ELEMENT;
			break;
			
		// We are waiting for an element
		case STATE_AWAITING_ELEMENT:
			if(!name.equals("element")) {
				throw new SAXException("Expected tag <element> got <"+name+">");
			}
			String key = attributes.getValue("key");
			String type = attributes.getValue("type");
			if(type == null) {
				throw new SAXException("<object> tag should contain both an id and class name");
			}
			break;
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
		
	}
	
	/**
	 * Convert a data string to the current type
	 * 
	 * @param data
	 * @return
	 */
	private Object toObjectHelper(String data) {
//		if(type.equals(TAG_INTEGER)) {
//			return Integer.parseInt(data);
//		} else if(type.equals(TAG_STRING)) {
//			return data;
//		} else if(type.equals(TAG_OBJREF)){
//			// Placeholder data
//			return null;				
//		} else if(type.equals(TAG_BOOLEAN)) {
//			return data.equals("true");
//		} else if(type.equals(TAG_LONG)) {
//			return Long.parseLong(data);
//		} else if(type.equals(TAG_DOUBLE)) {
//			return Double.parseDouble(data);
//		} else if(type.equals(TAG_ENUM)) {
//			return Enum.valueOf((Class<? extends Enum>) enumClass, data);
//		} else {
//			System.err.println("Unknown type "+type);
//		}
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
