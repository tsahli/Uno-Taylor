package unoClient;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONObject;

import uno.Card;

public class Client {

	public static void main(String[] args) throws IOException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new Client(new Socket());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Client(Socket socket) throws IOException {
		try {
			socket.connect(new InetSocketAddress("localhost", 9886), 9886);
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}

		PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

//		JSONObject js = new JSONObject(
//				"{\"type\": \"login\", \"message\":{\"username\":\"Tenzin\",\"password\":\"123456\"}}");
//		writer.write(js.toString() + "\n");
//
//		writer.flush();

		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		// sending play message to the server right after logging in.
		JSONObject jsMessage = new JSONObject(
				"{\"type\": \"application\", \"message\":{\"module\":\"Uno\",\"action\":\"play\"}}");
		writer.write(jsMessage.toString() + "\n");

		boolean gamePlay = true;

		while (gamePlay) {
			JSONObject message = new JSONObject(br.readLine());
			
			//System.out.println(messageObject);
			
			if (message.getString("type").equalsIgnoreCase("deny")) {
				System.out.println("access denied");
				break;
			}

			//JSONObject message = messageObject.getJSONObject("message");

			if (message.has("type")) {
				String type = message.getString("type");
				switch (type) {
				case ("reset"):
					// reset game
					break;
				}
			}

			if (message.has("action")) {
				String action = message.getString("action");

				switch (action) {
				case ("playedCard"):
					// play
					break;

				case ("start"):
					if (message.has("players")) {
						JSONArray playersMessage = message.getJSONArray("players");
						for (Object p : playersMessage) {
							if (p instanceof JSONObject) {
								JSONObject player = (JSONObject) p;
								int numOfCards = (int) player.get("cards");
								for (int i = 0; i < numOfCards; i++) {
									// user name display
								}
							}
						}
						// users turn show up
					}
					JSONObject cardMessage = new JSONObject(message.getString("card"));

					Card card = new Card("", Card.Color.Green);
					// pile update
					break;

				case ("drawnCard"):
					String playersTurn = message.getString("user");
					break;

				case ("turnMessage"):
					
					break;
					
				case ("callUno"):
					String unoCaller = message.getString("user");
					break;

				case ("win"):
					String winner = message.getString("username");
					break;

				case ("quit"):
					String quitter = message.getString("username");
					break;
				}
			}
		}
	}
}