package unoConsoleUpto3ComputerPlayers;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author 
 * CS 3230 
 * July 17, 2019
 */
public class Deck {
	private ArrayList<Card> deck;

	public Deck() {
		deck = new ArrayList<Card>();
		String[] colors = { "red", "blu", "grn", "ylw" };
		int[] numbers = { 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 0 }; // regular cards
		int[] specialnumbers = { 2, 2, 4, 4 }; // special cards +2, +2, +4 and +4
		
		for (String c : colors) { // adding regular cards to the deck
			for (int i : numbers) {
				deck.add(new Card(i, c.charAt(0))); // adding new cards to our deck
			}
		}
		
		for (int i : specialnumbers) { // adding special cards to the deck
			deck.add(new Card(i, 's'));
		}
	}

	public Deck(ArrayList<Card> c) { 
		deck = c;
	}

	public boolean isEmpty() { 
		if (deck.size() > 0) {
			return false;
		}
		return true;
	}

	public void shuffle() {
		Collections.shuffle(deck);
	}

	public Card getTopCard() {
		return deck.remove(deck.size() - 1);
	}

	public Card peek() {
		return deck.get(deck.size() - 1);
	}

	public String toString() {
		String deck = "";
		for (Card c : this.deck) {
			deck = deck + c + " ";
		}
		return deck;
	}
}