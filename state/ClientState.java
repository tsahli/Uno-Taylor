package state;

import cards.Card;
import player.Hand;

public class ClientState {
	public Hand hand;
	public Card topCard;
	public boolean gameEnded = false;
	
	public ClientState() {
		hand = new Hand();
	}
}
