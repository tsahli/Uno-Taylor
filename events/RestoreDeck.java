package events;

import java.util.Random;

import cardState.Card;
import cardValue.Wild;
import state.ClientState;
import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/8/2019
 * @description Server event for restoring and then re-shuffling the deck.
 */
public class RestoreDeck extends GameEvent {
	private static final long serialVersionUID = 1L;

	@Override
	public void subMakePrivate() {
	}

	@Override
	public GameEvent clone() {
		RestoreDeck e = new RestoreDeck();
		copyParentAttrs(e);
		return e;
	}

	@Override
	public String makeString() {
		return "Deck nearly depleted; took all but top card from pile, re-shuffled, and added back to deck.";
	}

	@Override
	public void doEventServer(ServerState gs) {
		if (!gs.topCard.equals(gs.pile.pop())) {
			System.err.println("topCard is not the first card on the pile!");
		}

		// Put the rest of the pile into the deck in a random order
		Random rand = new Random();
		int randNum;
		Card c;
		while (gs.pile.size() > 0) {
			randNum = rand.nextInt(gs.pile.size());
			c = gs.pile.get(randNum);
			// Reset the "color" of Wild / Wild Draw Four cards
			if (c.type == Card.WILD) {
				c = new Wild();
			}
			if (c.type == Card.WILDDRAWFOUR) {
				c = new Wild();
			}
			gs.deck.add(c);
			gs.pile.remove(randNum);
		}
		
		// Now put the top card back on the pile
		gs.pile.push(gs.topCard);
	}

	@Override
	public void doEventClient(ClientState gs) {
	}
}
