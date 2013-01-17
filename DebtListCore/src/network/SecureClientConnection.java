package network;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * A secure implementation of ClientConnection using SSLSocket instead of the ordinary Socktet
 */
public class SecureClientConnection extends ClientConnection {

	private SSLSocketFactory factory;
	
	public SecureClientConnection() {
		factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
	}
	
	@Override
	public Socket createSocket(String host, int port) throws IOException {
		SSLSocket sock = (SSLSocket) factory.createSocket(host, port);
		
		return sock;
	}
}
