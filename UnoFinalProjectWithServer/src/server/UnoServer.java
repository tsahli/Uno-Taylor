package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import events.DrawCard;
import events.FirstCard;
import events.GameEnd;
import events.GameEvent;
import events.RestoreDeck;
import events.PlayerTurn;
import events.PlayerTurnAfterDraw;
import player.Player;
import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/8/2019
 * @description Uno Server lets 2 to 10 number of players.
 * 
 *   An UNO deck contains:
 *   19 Blue Cards - 0 x1 and 1 to 9 x2
 *   19 Green Cards - 0 x1 and 1 to 9 x2
 *   19 Red Cards - 0 x1 and 1 to 9 x2
 *   19 Yellow Cards - 0 x1 and 1 to 9 x2
 *   8 Draw Two cards - 2 each in Blue, Green, Red and Yellow
 *   8 Reverse Cards - 2 each in Blue, Green, Red and Yellow
 *   8 Skip Cards - 2 each in Blue, Green, Red and Yellow
 *   4 Wild Cards
 *   4 Wild Draw Four cards
 */
public class UnoServer {
	
	private ServerState gs;
	private GameEventHandler events;
	private ServerSocket serverSocket = null;

	public static void main(String[] args) {
		UnoServer server = new UnoServer();
		try {
			server.runServer(9886);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private void runServer(int server_port) throws UnknownHostException {

		try {
			serverSocket = new ServerSocket(server_port);
		} catch (IOException e) {
			System.err.println("Server could not listen on port " + String.valueOf(server_port));
			System.exit(-1);
		}
		System.out.println(
				"Host IP: " + InetAddress.getLocalHost() + "\nServer listening on port " + String.valueOf(server_port));

		int numPlayers = 0;

		try {
			BufferedReader serverConsoleInput = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Please enter the number of players: ");
			try {
				String numPlayersStr = serverConsoleInput.readLine();
				numPlayers = Integer.valueOf(numPlayersStr).intValue();
			} catch (NumberFormatException e) {
				System.err.println("The number of players must be a number!");
				System.exit(-1);
			}
			if (numPlayers < 2 || numPlayers > 10) {
				System.err.println("The number of players must be between 2 and 10");
				System.exit(-1);
			}
			System.out.println("Server will wait for " + String.valueOf(numPlayers) + " players");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		gs = new ServerState();
		events = new GameEventHandler(gs);

		boolean serverRunning = true;
		while (serverRunning) {
			try {
				// Accept connections from players
				gs.setNumPlayers(numPlayers);
				System.out.println("\nWaiting for connections...");
				int playersJoined = 0;
				while (playersJoined < numPlayers) {
					Socket newSocket = serverSocket.accept();
					// System.out.println("Got connection");
					gs.newPlayer(newSocket);
					playersJoined++;
				}
				System.out.println("\nAll players joined");

				// Set up game
				gs.prepareForGameStart();
				events.handleEvent(new FirstCard(gs.topCard));
				for (Player p : gs.player) {
					events.handleEvent(new DrawCard(p, 7));
				}

				// Start game
				boolean gameActive = true;
				GameEvent e;
				while (gameActive) {
					if (gs.currentPlayerDrewUsableCard) {
						gs.currentPlayerOutputStream().writeObject(new PlayerTurnAfterDraw(gs.drawnUsableCard));
						gs.currentPlayerDrewUsableCard = false;
						gs.drawnUsableCard = null;
					} else {
						gs.currentPlayerOutputStream().writeObject(new PlayerTurn());
					}
					e = (GameEvent) gs.currentPlayerInputStream().readObject();
					events.handleEventFromPlayer(e, gs.currentPlayer);
					GameEvent e2;
					while (!gs.eventBuffer.isEmpty()) {
						e2 = gs.eventBuffer.poll();
						events.handleEvent(e2);
					}
					if (gs.activePlayers() == 1) {
						events.handleEvent(new GameEnd());
						gameActive = false;
					} else {
						if (gs.deck.size() < 5) {
							events.handleEvent(new RestoreDeck());
						}

						if (!gs.currentPlayerDrewUsableCard)// If the player picked up a card that they can put down,
															// DON'T move to the next player yet
							gs.moveToNextPlayer();
					}
				}

				for (ObjectOutputStream s : gs.playerOutputs) {
					s.close();
				}
				for (ObjectInputStream s : gs.playerInputs) {
					s.close();
				}
				for (Socket s : gs.playerSockets) {
					if (s != null)
						s.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server has been closed.");
	}

	private class GameEventHandler {
		public ServerState gs;

		public GameEventHandler(ServerState gs) {
			this.gs = gs;
		}

		public void handleEventFromPlayer(GameEvent e, Player p) {
			e.setPlayer(p);
			handleEvent(e);
		}

		public void handleEvent(GameEvent e) {
			e.doEventServer(gs);
			logEvent(e);
			broadcastEvent(e);
		}

		public void logEvent(GameEvent e) {
			if (e.makeString().equals("NullEvent"))
				return;
			System.out.println(e.toString());
		}

		public void broadcastEvent(GameEvent e) {
			if (e.makeString().equals("NullEvent"))
				return;

			GameEvent eventYou = e.clone();
			GameEvent eventOther = e.clone();
			Player player = e.player;

			eventYou.makeYou();
			eventOther.makePrivate();

			try {
				for (int i = 0; i < gs.totalPlayers; i++) {
					if (gs.player[i] == player) {
						gs.playerOutputs[i].writeObject(eventYou);
					} else {
						gs.playerOutputs[i].writeObject(eventOther);
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}