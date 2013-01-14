package requests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import requests.xml.XMLSerializable;
import session.PCSession;

import logic.Debt;

public class UpdateListener implements Runnable {

	private int port;

	public UpdateListener(int port) {
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
			sock = ss.accept();
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			writer = new PrintWriter(sock.getOutputStream(), true);
			while(true) {
				// TODO: Does this need to be in a separate thread?
				// Will the server send many updates at once?
				// Will it use the same connection?
				PCSession.session.processUpdate(XMLSerializable.toObject(reader.readLine()));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			System.out.println("Closing UpdateListener");
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
