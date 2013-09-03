package logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogWriter {

	private PrintWriter writer;
	
	public LogWriter(String pathToLogFile) {
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(pathToLogFile, true)));
		} catch (IOException e) {
			System.out.println("Failed to create logger! " + e);
		}
	}
	
	public synchronized void write(String msg) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String df = sdf.format(Calendar.getInstance().getTime());
		writer.println(df + ": " + msg);
		writer.flush();
	}
}
