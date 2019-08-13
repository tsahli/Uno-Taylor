package events;

import java.util.Random;

import cards.Card;
import cards.WildCard;
import state.ClientState;
import state.ServerState;

public class ReplenishDeck extends GameEvent {
	private static final long serialVersionUID = 1L;

	@Override
	public void subMakePrivate() {
	}

	@Override
	public GameEvent clone() {
		ReplenishDeck e = new ReplenishDeck();
		copyParentAttrs(e);
		return e;
	}

	@Override
	public String makeString() {
		return "Deck nearly depleted; took all but top card from pile, re-shuffled, and added back to deck.";
	}

	@Override
	public void doEventServer(ServerState gs) {
		// Pop off the first card - this stays as the only
		// card in the "pile"
		if (!gs.topCard.equals(gs.pile.pop())) {
			System.err.println("topCard is not the first card on the pile!");
		}

		// Put the rest of the pile into the deck,
		// in a random order
		Random rand = new Random();
		int randNum;
		Card c;
		while (gs.pile.size() > 0) {
			randNum = rand.nextInt(gs.pile.size());
			c = gs.pile.get(randNum);
			// Reset the "color" of Wild / Wild Draw Four cards
			if (c.type == Card.WILD) {
				c = new WildCard();
			}
			if (c.type == Card.WILDDRAWFOUR) {
				c = new WildCard();
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
