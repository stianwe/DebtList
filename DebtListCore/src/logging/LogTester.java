package logging;

public class LogTester {

	public static void main(String[] args) {
		Logger logger = new LogBuilder("testLog.log").createLogger("LogTester");
		logger.sendToLog(LogLevel.VERBOSE, "0", "Verbose log entry with tag 0.");
		logger.sendToLog(LogLevel.HIGH, "1337", "High log entry with tag 1337.");
	}
}
