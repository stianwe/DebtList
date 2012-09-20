package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class ServerConnection {

	public static final int SERVER_PORT = 1337;
	
	private List<ServerConnectionHandler> handlers;
	
	public ServerConnection() {
		this.handlers = new ArrayList<ServerConnectionHandler>();
	}
	
	public synchronized void addConnectionHandler(ServerConnectionHandler handler) {
		this.handlers.add(handler);
	}
	
	public synchronized void removeConnectionHandler(ServerConnectionHandler handler) {
		this.handlers.remove(handler);
	}
	
	public void accept(int port) throws IllegalArgumentException {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
			while(true) {
				new ServerConnectionHandler(ss.accept(), this).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				ss.close();
			} catch (Exception e) {}
		}
	}
}
