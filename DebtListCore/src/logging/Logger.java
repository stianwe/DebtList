package logging;

public class Logger {
	
	private String source;
	
	private LogWriter logWriter;
	
	public Logger(String source, LogWriter logWriter) {
		this.source = source;
		this.logWriter = logWriter;
	}
	
	public void sendToLog(LogLevel level, String tag, String message) {
		logWriter.write(level + ":\t" + tag + ":\t" + source + ":\t" + message);
	}
}
