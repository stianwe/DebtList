package network;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class UpdateSender implements Runnable{

	private Queue<String> sendQueue;
	private Socket socket = null;
	private boolean running;
	private PrintWriter writer;
	
	public UpdateSender(String host, int port) {
		running = false;
		sendQueue = new LinkedList<String>();
		try {
			socket = new Socket(host, port);
			System.out.println("UpdateSender connected to client.");
			writer = new PrintWriter(socket.getOutputStream(), true);
		} catch(Exception e) {
			System.out.println("Connection failed: " + e);
		}
	}

	public synchronized int getQueueSize() {
		return sendQueue.size();
	}
	
	public synchronized void send(String msg) {
		sendQueue.add(msg);
		if(sendQueue.size() == 1) notifyAll();
	}
	
	public synchronized String getNextStringToSend() {
		return sendQueue.poll();
	}
	
	public void setRunning(boolean isRunning) {
		this.running = isRunning;
	}
	
	@Override
	public void run() {
		running = true;
		while(running) {
			String toSend = null;
			if((toSend = getNextStringToSend()) != null) {
				System.out.println("UPDATE!");
				System.out.println(socket.getInetAddress().getHostAddress());
				writer.println(toSend);
			} else {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {}
			}
		}
		System.out.println("UpdateSender stopping.");
	}
	
	
	
}
