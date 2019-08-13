package state;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import cards.Card;
import cards.Color;
import cards.DrawTwoCard;
import cards.NumberCard;
import cards.ReverseCard;
import cards.SkipCard;
import cards.WildCard;
import cards.WildDrawFourCard;
import events.GameEvent;
import player.Player;

public class ServerState {
	public Player[] player;
	public Socket[] playerSockets;
	public ObjectInputStream[] playerInputs;
	public ObjectOutputStream[] playerOutputs;
	public Color[] allColors;
	public LinkedList<Card> deck;
	public LinkedList<Card> pile;
	public Card topCard;
	public Player currentPlayer;
	private int currentPlayerId;
	public int totalPlayers;
	public boolean playDirection;
	public Queue<GameEvent> eventBuffer;
	public int nextFinishPosition;
	public boolean skipNextPlayer;
	public boolean currentPlayerDrewUsableCard;
	public Card drawnUsableCard;
	
	public void setNumPlayers(int numPlayers) {
		totalPlayers = numPlayers;
		player = new Player[totalPlayers];
		playerSockets = new Socket[totalPlayers];
		playerInputs = new ObjectInputStream[totalPlayers];
		playerOutputs = new ObjectOutputStream[totalPlayers];
	}
	
	public void newPlayer(Socket newSocket) {
		int i;
		for (i=0; i<totalPlayers; i++) {
			if (player[i] == null)
				break;
		}
		if (i>=totalPlayers) {
			System.err.println("Too many players, exiting...");
			System.exit(-1);
		}
		try {
			playerSockets[i] = newSocket;
			playerInputs[i] = new ObjectInputStream(newSocket.getInputStream());
			playerOutputs[i] = new ObjectOutputStream(newSocket.getOutputStream());
			String name = (String) playerInputs[i].readObject();
			player[i] = new Player(name);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void moveToNextPlayer() {
		currentPlayerId = nextPlayerId();
		currentPlayer = player[currentPlayerId];
	}
	
	public ObjectInputStream currentPlayerInputStream() {
		return playerInputs[currentPlayerId];
	}

	public ObjectOutputStream currentPlayerOutputStream() {
		return playerOutputs[currentPlayerId];
	}

	public Player nextPlayer() {
		return player[nextPlayerId()];
	}
	
	public int nextPlayerId() {
		int nextPlayerId = currentPlayerId;
		do {
			if (playDirection) {
				nextPlayerId = nextPlayerId + 1;
				if (nextPlayerId >= totalPlayers)
					nextPlayerId = 0;
			} else {
				nextPlayerId = nextPlayerId - 1;
				if (nextPlayerId < 0)
					nextPlayerId = totalPlayers - 1;
			}
		} while (player[nextPlayerId].finished);
		if (skipNextPlayer) {
			// Go past this player to the next one
			skipNextPlayer = false;
			currentPlayerId = nextPlayerId;
			return nextPlayerId();
		} else {
			return nextPlayerId;
		}
	}
	
	public void changePlayDirection() {
		if (activePlayers() == 2)
			skipNextPlayer = true;
		else
			playDirection = !playDirection;
	}
	
	public Player getPlayerByName(String pName) {
		Player p = null;
		for (Player p2 : this.player) {
			if (p2.name == pName) {
				p = p2;
				break;
			}
		}
		return p;
	}
	
	public int activePlayers() {
		int activePlayers = 0;
		for (Player p : player) {
			if (!p.finished)
				activePlayers++;
		}
		//System.out.println("Active players: " + activePlayers);
		return activePlayers;
	}
	
	public void prepareForGameStart() {
		eventBuffer = new LinkedList<GameEvent>();
		currentPlayerId = 0;
		currentPlayer = player[currentPlayerId];		
		allColors = new Color[]{Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW};
		nextFinishPosition = 1;
		skipNextPlayer = false;
		playDirection = false;
		currentPlayerDrewUsableCard = false;
		drawnUsableCard = null;
		
		// Create deck
		/*
		 * An UNO deck contains:
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
		LinkedList<Card> tempCards = new LinkedList<Card>();
		for (Color c : allColors) {
			tempCards.add(new NumberCard(0, c));
			for (int j=0; j<2; j++) {
				for (int i=1; i<10; i++) {
					tempCards.add(new NumberCard(i, c));
				}
				tempCards.add(new DrawTwoCard(c));
				tempCards.add(new ReverseCard(c));
				tempCards.add(new SkipCard(c));
			}
		}
		for (int j=0; j<4; j++) {
			tempCards.add(new WildCard());
			tempCards.add(new WildDrawFourCard());
		}
		
		// Shuffle deck
		deck = new LinkedList<Card>();
		Random rand = new Random();
		int randNum;
		while (tempCards.size() > 0) {
			randNum = rand.nextInt(tempCards.size());
			deck.add(tempCards.get(randNum));
			tempCards.remove(randNum);
		}
		//System.out.println(deck.size() + " cards in deck");
		
		// Create pile and turn over first card
		// Ensure first card is a number card
		pile = new LinkedList<Card>();
		do {
			topCard = deck.pop();
			pile.push(topCard);
		} while (topCard.type != Card.NUMBER);
	}
}
