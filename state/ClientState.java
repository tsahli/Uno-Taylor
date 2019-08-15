package state;

import cardState.Card;
import player.Hand;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/8/2019
 * @description Client's first state with initializing of its first hands(7 cards). 
 */
public class ClientState {
	public Hand hand;
	public Card topCard;
	public boolean gameEnded = false;
	
	public ClientState() {
		hand = new Hand();
	}
}
