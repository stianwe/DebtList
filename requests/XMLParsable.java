package requests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.modelmbean.XMLParseException;

import logic.Debt;
import logic.User;


public abstract class XMLParsable {

	private List<Tuple> variables;
	
	public void addVariable(String variableName, Object variable) {
		if(variables == null) {
			this.variables = new ArrayList<Tuple>();
		} else {
			for (int i = 0; i < variables.size(); i++) {
				if(variables.get(i).getVariableName().equals(variableName)) {
					variables.remove(i);
					break;
				}
			}
		}
		variables.add(new Tuple(variableName, variable));
	}
	
	public abstract String getClassName();
	
	public int getNumberOfVariables() {
		return variables.size();
	}
	
	public String getVariableName(int i) {
		return variables.get(i).getVariableName();
	}
	
	public Object getVariable(String varName) {
		for (Tuple t : variables) {
			if(t.getVariableName().equals(varName)) return t.getVariable();
		}
		return null;
	}
	
	public Object getVariable(int i) {
		return variables.get(i).getVariable();
	}
	
	public void removeVariable(int i) {
		variables.remove(i);
	}
	
	public void removeVariable(String variableName) {
		Tuple toBeRemoved = null;
		for (Tuple t : variables) {
			if(t.getVariableName().equals(variableName)) {
				toBeRemoved = t;
			}
		}
		if(toBeRemoved != null) {
			variables.remove(toBeRemoved);
		}
	}
	
	public void updateVariable(String variableName, Object variable) {
		removeVariable(variableName);
		addVariable(variableName, variable);
	}
	
	public String toXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<xml>");
		sb.append(buildXml(this));
		sb.append("</xml>");
		return sb.toString();
	}
	
	private static String buildXml(XMLParsable p) {
		StringBuilder sb = new StringBuilder();
		sb.append("<" + p.getClassName() + ">");
		for (int i = 0; i < p.getNumberOfVariables(); i++) {
			if(p.getVariable(i) instanceof List) {
				List l = (List) p.getVariable(i);
				sb.append("<" + p.getVariableName(i) + "><List>");
				for (Object o : l) {
					if(o instanceof XMLParsable) {
						sb.append(buildXml((XMLParsable) o));
					} else {
						throw new RuntimeException("Error while parsing a list (XML)!");
					}
				}
				sb.append("</List></" + p.getVariableName(i) + ">");
//			} else if (p.getVariable(i) instanceof XMLParsable) {
//				sb.append(buildXml((XMLParsable) p.getVariable(i)));
//			} else {
//				sb.append("<" + p.getVariableName(i) + ">" + p.getVariable(i) + "</" + p.getVariableName(i) + ">");
//			}
			} else {
				sb.append("<" + p.getVariableName(i) + ">" + (p.getVariable(i) instanceof XMLParsable ? buildXml((XMLParsable) p.getVariable(i)) : p.getVariable(i)) + "</" + p.getVariableName(i) + ">");
			}
		}
		sb.append("</" + p.getClassName() + ">");
		return sb.toString();
	}
	
	public static Object toObject(String xml) {
		// Remove surrounding xml-tags
		xml = splitOuter(xml).s2;
		return toObjectHelper(xml);
	}
	
	private static Object toObjectHelper(String xml) {
		// Remove surrounding class-tags
		StringPair temp = splitOuter(xml);
		String className = temp.s1;
		xml = temp.s2;
		List<Tuple> variables = splitInner(xml);
		Map<String, Object> vars = new HashMap<String, Object>();
		for (Tuple t : variables) {
			if(shouldSplitOuter((String)t.getVariable())) {
				String innerClassName = splitOuter((String) t.getVariable()).s1;
				System.out.println("Outer was: " + innerClassName);
				if(innerClassName.equalsIgnoreCase("list")) {
					System.out.println("Trying to put in list: " + ((String) t.getVariable()));
					vars.put(t.getVariableName(), toList(((String) t.getVariable())));
				} else {
					vars.put(t.getVariableName(), toObjectHelper((String) t.getVariable()));
					System.out.println(t.getVariableName() + ": " + innerClassName + ": " + t.getVariable());
//					throw new RuntimeException("SOMETHING WRONG HAPPENED WHILE PARSING XML! VarName=" + splitOuter((String) t.getVariable()).s1);
				}
			} else {
				vars.put(t.getVariableName(), (String)t.getVariable());
			}
		}
		return assembleObject(className, vars);
		
	}

	/**
	 * Parses XML on the form "<ListName><List><attributtes></List></ListName>" to a list
	 * 
	 * @param xml	The XML as a String
	 * @return		The list
	 */
	private static List<Object> toList(String xml) {
		List<Object> l = new ArrayList<Object>();
		// remove list-tag
		xml = splitOuter(xml).s2;
		// find name of list
//		StringPair temp = splitOuter(xml);
//		String listName = temp.s1;
//		xml = temp.s2;
		//
		System.out.println("XML in list: " + xml);
		List<Tuple> objects = splitInner(xml);
		for (Tuple t : objects) {
			System.out.println(t.getVariableName() + ": " + t.getVariable());
			List<Tuple> variables = splitInner((String) t.getVariable());
			Map<String, Object> vars = new HashMap<String, Object>();
			for (Tuple t2 : variables) {
				vars.put(t2.getVariableName(), (String)t2.getVariable());
			}
			l.add(assembleObject(t.getVariableName(), vars));
		}
		return l;
	}
	
	private static Object assembleObject(String className, Map<String, Object> vars) {
		Object o = null;
		switch(className) {
		case "LogInRequest":
//			o = new LogInRequest((String)vars.get("username"), (String)vars.get("password"), ((String)vars.get("accepted")).equals("true") ? true : false, LogInRequestStatus.values()[Integer.parseInt((String) vars.get("status"))]);
			o = new LogInRequest((User) vars.get("uUser"), ((String)vars.get("accepted")).equals("true") ? true : false, LogInRequestStatus.values()[Integer.parseInt((String) vars.get("status"))], Integer.parseInt((String) vars.get("updatePort")));
			break;
		case "User":
			if(vars.containsKey("friends")) {
				o = new User((String) vars.get("username"), (String) vars.get("password"), (List<User>) vars.get("friends"));
			} else {
				o = new User((String) vars.get("username"), (String) vars.get("password"));
			}
			break;
		case "CreateUserRequest":
			o = new CreateUserRequest((User) vars.get("requestedUser"), ((String) vars.get("isApproved")).equals("true") ? true : false);
			break;
		case "Debt":
			o = new Debt(Long.parseLong((String) vars.get("id")), Double.parseDouble((String) vars.get("amount")), (String) vars.get("what"), (User) vars.get("from"), (User) vars.get("to"), (String) vars.get("comment"), (User) vars.get("requestedBy"));
			break;
		default:
			throw new RuntimeException("SOMETHING WENT WRONG WHEN ASSEMBLING OBJECT! className=" + className);
		}
		System.out.println("Assembled: " + o);
		return o;
	}
	
	private static StringPair splitOuter(String xml) {
		int temp = xml.indexOf('>');
		String attName = xml.substring(1, temp);
		int temp2 = xml.lastIndexOf("</" + attName + ">");
		String inner = xml.substring(temp + 1, temp2);
		if((temp2 + 3 + attName.length()) < xml.length()) {
			// This was not an outer split
			return null;
		}
		return new StringPair(attName, inner);
	}
	
	private static List<Tuple> splitInner(String xml) {
		List<Tuple> variables = new ArrayList<Tuple>();
		int temp;
		while((temp = xml.indexOf('>')) > -1) {
			String attName = xml.substring(1, temp);
			int temp2 = xml.indexOf("</" + attName + ">");
			String inner = null;
			try {
				inner = xml.substring(temp + 1, temp2);
			} catch (Exception e) {
				System.out.println("Was looking for attName: " + attName + " in " + xml);
				throw e;
			}
			System.out.println("Looking for attName: " + attName + " in " + xml);
			xml = xml.substring(temp2 + attName.length() + 3);
			variables.add(new Tuple(attName, inner));
		}
		return variables;
	}
	
	public static void main(String[] args) {
		User stian = new User("Stian");
		User arne = new User("Arne");
		Debt d = new Debt(1, 2, "s", arne, stian, "asd", arne);
		stian.addPendingDebt(d);
		arne.addPendingDebt(d);
		LogInRequest r = new LogInRequest((User) stian.toSendable(true), true, LogInRequestStatus.ACCEPTED, 13338);
//		System.out.println("XML: " + r.toXml());
//		Object o = toObject(r.toXml());
		Object u = toObject(stian.toSendable(true).toXml());
	}
	
	private static boolean shouldSplitOuter(String xml) {
		try {
			int temp = xml.indexOf('>');
			String attName = xml.substring(1, temp);
			int temp2 = xml.lastIndexOf("</" + attName + ">");
			return temp2 + 3 + attName.length() >= xml.length();
		} catch (Exception e) {
			return false;
		}
	}
	
	static class StringPair {
		public String s1, s2;
		
		public StringPair(String s1, String s2) {
			this.s1 = s1;
			this.s2 = s2;
		}
	}
}
