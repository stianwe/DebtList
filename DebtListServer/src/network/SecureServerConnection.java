package network;

import java.io.IOException;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocketFactory;

/**
 * A secure implementation of ServerConnection using SSLServerSocket instead of the ordinary ServerSocket
 */
public class SecureServerConnection extends ServerConnection {

	private SSLServerSocketFactory factory;
	
	public SecureServerConnection(boolean readFromDatabase) {
		super(readFromDatabase);
		factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
	}
	
	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		return factory.createServerSocket(port);
	}
}
