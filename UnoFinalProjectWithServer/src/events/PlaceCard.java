package events;

import cardState.Card;
import state.ClientState;
import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/8/2019
 * @description Event for placing a card on the pile of used cards 
 */
public class PlaceCard extends GameEvent {
	private static final long serialVersionUID = 1L;
	public Card cardPlayed;
	
	public PlaceCard(Card c) {
		this.cardPlayed = c;
	}

	@Override
	public void subMakePrivate() {
	}

	@Override
	public String makeString() {
		return playerName + " played " + cardPlayed.toString();
	}

	@Override
	public GameEvent clone() {
		PlaceCard e = new PlaceCard(cardPlayed);
		copyParentAttrs(e);
		return e;
	}

	@Override
	public void doEventServer(ServerState gs) {
		// Check whether the user has this card
		if (!player.hand.remove(cardPlayed)) {
			System.err.println("Player played card they don't have!");
			System.err.println(player.name);
			System.err.println(cardPlayed.makeString());
			System.err.println(player.hand.printHand());
			System.exit(-1);
		}
		
		// Validate that this card can be played
		if (!cardPlayed.canPlaceOn(gs.topCard)) {
			System.err.println("Player played card that can't be placed on top of current card!");
			System.err.println(player.name);
			System.err.println(cardPlayed.makeString());
			System.err.println(gs.topCard.makeString());
			System.exit(-1);
		}
		
		// Play the card
		gs.pile.push(cardPlayed);
		gs.topCard = cardPlayed;
		cardPlayed.doCardAction(gs);
		
		if (player.hand.cards.size() == 1) {
			gs.eventBuffer.add(new CallUno(player));
		} else if (player.hand.cards.size() == 0) {
			gs.eventBuffer.add(new PlayerFinished(player));
		}
	}

	@Override
	public void doEventClient(ClientState gs) {
		if (this.isYou) {
			gs.hand.remove(cardPlayed);
		}
		gs.topCard = cardPlayed;
	}
}
