package requests.xml;

import java.io.IOException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import logic.Debt;
import logic.DebtStatus;
import logic.User;

import org.junit.Test;

public class XMLSerializableTest extends TestCase {

	private User u1, u2, simpleUser;
	private Debt d1, d2, d3;
	
	public void setUp() {
		u1 = new User(1, "User1");
		u2 = new User(2, "User2");
		d1 = new Debt(1, 12, "tests", u1, u2, "Comment in debt 1", u1, DebtStatus.REQUESTED);
		d2 = new Debt(2, 3, "ingen ting, Tingeling", u2, u1, "Comment d2", u2, DebtStatus.REQUESTED);
		d3 = new Debt(3, 3, "nothings", u1, u2, "test", u2, DebtStatus.CONFIRMED);
		u1.addPendingDebt(d1);
		u2.addPendingDebt(d1);
		u1.addPendingDebt(d2);
		u2.addPendingDebt(d2);
		u1.addConfirmedDebt(d3);
		u2.addConfirmedDebt(d3);
		// Empty user
		simpleUser = new User(3, "EmptyUser");
	}
	
	public static junit.framework.Test suite() {
		return new TestSuite(XMLSerializableTest.class);
	}
	
	public XMLSerializableTest() {
		setUp();
		try {
			User parsed = (User) XMLSerializable.toObject(u1.toXML());
			System.out.println(parsed);
			System.out.println("Pending debts: " + parsed.getPendingDebts());
			System.out.println(parsed.getPendingDebt(0));
			System.out.println(parsed.getPendingDebt(1));
			System.out.println("Confirmed debts: " + parsed.getConfirmedDebts());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		XMLSerializableTest t = new XMLSerializableTest();
	}
	
	@Test
	public void testToObject() {
		try {
			User parsedSimpleUser = (User) XMLSerializable.toObject(simpleUser.toXML());
			assertEquals("simpleUser id test", simpleUser.getId(), parsedSimpleUser.getId());
			assertEquals("simpleUser username test", simpleUser.getUsername(), parsedSimpleUser.getUsername());
		} catch (IOException e) {
			fail("Parsing simpleUser to xml and back again threw an exception");
		}
		try {
			User parsedU1 = (User) XMLSerializable.toObject(u1.toXML());
			assertEquals("u1 first pending debt comment test", u1.getPendingDebt(1).getComment(), parsedU1.getPendingDebt(0).getComment());
			assertEquals("u1 first confirmed debt comment test", u1.getConfirmedDebt(0).getComment(), parsedU1.getConfirmedDebt(0).getComment());
		} catch (IOException e) {
			fail("Parsing u1 to xml and back again threw an exception");
		}
	}

}
