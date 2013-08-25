package logging;

public enum LogLevel {

	/**
	 * The highest possible log level,
	 * used to indicate that an error 
	 * has occurred.
	 * Might be used to log an event that
	 * breaks the current instance
	 */
	ERROR, 
	
	/**
	 * The second highest possible log
	 * level, used to indicate that 
	 * something unexpected has occurred,
	 * but that is not an error that will
	 * break anything
	 */
	UNEXPECTED, 
	
	/**
	 * The highest "normal" log level,
	 * used to indicate that something
	 * important has happened, but which
	 * was expected and that does not 
	 * break anything
	 */
	HIGH, 
	
	/**
	 * An average high log level,
	 * used to indicate that an event
	 * that might be worth monitoring
	 * has occurred, but that is not
	 * very important
	 */
	MEDIUM, 
	
	/**
	 * A low log level,
	 * used to indicate that an event
	 * which probably is not very important
	 * has happened
	 */
	LOW, 
	
	/**
	 * The lowest possible log level,
	 * used for logging events that 
	 * are not important, but might 
	 * be informational
	 */
	VERBOSE,;
}
