package player;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;

import cards.Card;

public class Hand implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public LinkedList<Card> cards;
	
	public Hand() {
		cards = new LinkedList<Card>();
	}
	
	public void add(Card c) {
		cards.add(c);
	}
	
	public boolean remove(Card c) {
		return cards.remove(c);
	}
	
	public String printHand() {
		return printHand(false);
	}
	
	public String printHand(boolean sort) {
		if (sort) {
			Collections.sort(this.cards);
		}
		String out = "";
		int i=1;
		for (Card c : this.cards) {
			out = out + String.valueOf(i) + ": " + c.makeString() + "\n";
			i++;
		}
		return out;
	}
}
