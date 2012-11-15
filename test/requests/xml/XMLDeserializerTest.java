package test.requests.xml;

import java.io.IOException;

import org.junit.Test;

import requests.xml.XMLConstants;
import requests.xml.XMLDeserializer;
import requests.xml.XMLSerializable;

public class XMLDeserializerTest implements XMLConstants {

	private static final String TEST_MODEL = TestModel.class.getName();
	
	private XMLSerializable deserialize(String xml) throws IOException{
		return XMLDeserializer.toObject(xml);
	}
	
	/**
	 * Tests the de-serialization of a set of simple types; String, Boolean,
	 * Integers, etc.
	 */
	@Test
	public void testSimpleTypes() {
		try {
			XMLSerializable obj = deserialize(
				"<"+TAG_OUTER+">" +
					"<"+TAG_OBJECT+" class=\""+TEST_MODEL+"\" id=\"dummy\">" +
						"<"+TAG_ELEMENT+" key=\" "
					
					);
		}
	}

}
