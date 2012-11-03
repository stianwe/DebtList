package requests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import session.Session;

import logic.Debt;

public class UpdateListener implements Runnable {

	private int port;

	public UpdateListener(int port) {
		System.out.println("UPDATELISTENER CREATED!!!!!!!!!!!!!!!!!!!!!");
		this.port = port;
	}
	
	@Override
	public void run() {
		ServerSocket ss = null;
		Socket sock = null;
		BufferedReader reader = null;
		PrintWriter writer = null;
		try {
			ss = new ServerSocket(port);
			while(true) {
				sock = ss.accept();
				reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				writer = new PrintWriter(sock.getOutputStream(), true);
				// TODO: Does this need to be in a separate thread?
				// Will the server send many updates at once?
				// Will it use the same connection?
				Session.session.processUpdate(XMLParsable.toObject(reader.readLine()));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				ss.close();
			} catch (Exception e) {}
			try {
				sock.close();
			} catch (Exception e) {}
			try {
				reader.close();
			} catch (Exception e) {}
			try {
				writer.close();
			} catch (Exception e) {}
		}
	}

}
