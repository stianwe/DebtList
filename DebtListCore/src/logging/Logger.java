package logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger {

	private PrintWriter writer;
	
	public Logger(String source, String pathToLogFile) {
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(pathToLogFile, true)));
		} catch (IOException e) {
			System.out.println("Failed to create logger! " + e);
		}
	}
	
	public void sendToLog(LogLevel level, String tag, String message) {
		write(level + ": " + tag + ": " + message);
	}
	
	private void write(String msg) {
		writer.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()) + ": " + msg);
	}
}
