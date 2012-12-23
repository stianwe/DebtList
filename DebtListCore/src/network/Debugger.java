package network;

public class Debugger {

	private static boolean debug = false;
	
	/**
	 * Prints the given message in the debugging channel
	 * @param message	The message
	 */
	public static void print(String message) {
		if(debug) System.out.println("[Debug] " + message);
	}
	
	public static void setDebug(boolean debug) {
		Debugger.debug = debug;
	}
}
