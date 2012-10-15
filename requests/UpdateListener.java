package requests;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class UpdateListener implements Runnable {
	
	private int port;
	
	public UpdateListener(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		while(true) {
			ServerSocket ss = null;
			try {
				ss = new ServerSocket(port);
				Socket sock = ss.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				
			}
		}
	}

}
