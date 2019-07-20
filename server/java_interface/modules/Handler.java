package modules;

import org.json.JSONObject;

public abstract class Handler implements Runnable {
	protected static boolean isValidPort(String portString) {
		int port;
		try {
			port = Integer.parseInt(portString);
		} catch (NumberFormatException e) {
			port = -1;
		}
		
		return port > 100 && port < 65531;
	}
	
	private IPCQueue ipc;
	
	public Handler(String portString) {
		int port = isValidPort(portString) ? Integer.parseInt(portString) : 8990;

		ipc = new IPCQueue(port);
	}

	protected void send(JSONObject json) {
		ipc.sendMessage(json);
	}
	
	protected void broadcast(JSONObject message, String module) {
		JSONObject json = new JSONObject();
		json.put("action", "broadcast");
		json.put("module",  module);
		json.put("message", message);
		
		send(json);
	}
	
	protected void netSend(JSONObject message, String username, String module) {
		JSONObject json = new JSONObject();
		json.put("action", "netSend");
		json.put("module",  module);
		json.put("username", username);
		json.put("message", message);
		
		send(json);
	}
	
	protected void changeLogLevel(String level, String module) {
		JSONObject json = new JSONObject();
		json.put("action", "update_log_level");
		json.put("module", module);
		json.put("level", level);
		
		send(json);
	}
	
	protected void log(String level, String message, String module) {
		JSONObject log = new JSONObject();
		log.put("action", "log");
		log.put("text", message);
		log.put("module", module);
		log.put("level", level);
		
		send(log);
	}
	
	@Override
	public void run() {
		while (true) {
			handle(ipc.getNextMessage());
		}
	}
	
	protected abstract void handle(JSONObject message);
}
