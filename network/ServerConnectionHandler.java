package network;

import java.net.Socket;

public class ServerConnectionHandler extends Thread {

	private Socket connection;
	private ServerConnection serverConnection;
	
	public ServerConnectionHandler(Socket connection, ServerConnection serverConnection) {
		this.connection = connection;
		this.serverConnection = serverConnection;
		serverConnection.addConnectionHandler(this);
	}
	
	@Override
	public void run() {
		// TODO
	}
	
}
