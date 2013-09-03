package logging;

public class LogBuilder {

	private LogWriter logWriter;
	
	public LogBuilder(String pathToLogFile) {
		logWriter = new LogWriter(pathToLogFile);
	}
	
	public Logger createLogger(String source) {
		return new Logger(source, logWriter);
	}
}
