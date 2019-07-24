package modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

public class IPCQueue {
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	
	public IPCQueue(int port) {
		try {
			socket = new Socket("localhost", port);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	protected void sendMessage(JSONObject json) {
		writer.println(json);
		writer.flush();
	}
	
	protected JSONObject getNextMessage() {
		String message = null;
		try {
			message = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		return new JSONObject(message);
	}
}
