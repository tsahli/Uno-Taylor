package events;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import cards.Card;
import player.Player;
import state.ClientState;
import state.ServerState;

public class DrawCard extends GameEvent {
	private static final long serialVersionUID = 1L;
	public int numCardsDrawn;
	public LinkedList<Card> cardsDrawn = null;
	public boolean insufficientCards = false;
	
	public DrawCard() {
		numCardsDrawn = 1;
	}
	
	public DrawCard(Player p, int numCardsDrawn) {
		this.setPlayer(p);
		this.numCardsDrawn = numCardsDrawn;
	}

	@Override
	public void subMakePrivate() {
		cardsDrawn = null;
	}

	@Override
	public String makeString() {
		String output = playerName + " drew " + String.valueOf(numCardsDrawn) + " card";
		if (numCardsDrawn != 1)
			output = output + "s";
		if (cardsDrawn != null) {
			output = output + ": ";
			for (Card c : cardsDrawn) {
				output = output + c.toString() + ", ";
			}
			output = output.substring(0, output.length() - 2);
		}
		if (insufficientCards) {
			output = output + " (insufficient cards in deck!)";
		}
		return output;
	}

	@Override
	public GameEvent clone() {
		DrawCard e = new DrawCard();
		e.numCardsDrawn = this.numCardsDrawn;
		e.cardsDrawn = this.cardsDrawn;
		e.insufficientCards = this.insufficientCards;
		copyParentAttrs(e);
		return e;
	}

	@Override
	public void doEventServer(ServerState gs) {
		Card card;
		cardsDrawn = new LinkedList<Card>();
		for (int i=0; i<numCardsDrawn; i++) {
			try {
				card = gs.deck.pop();
				cardsDrawn.add(card);
			} catch (NoSuchElementException e) {
				numCardsDrawn = cardsDrawn.size();
				insufficientCards = true;
			}
		}
		for (Card c : cardsDrawn) {
			player.hand.add(c);
		}
		// Handle the case where user picks up a
		// card and is able to play it immediately
		if (numCardsDrawn == 1) {
			if (cardsDrawn.get(0).canPlaceOn(gs.topCard)) {
				gs.currentPlayerDrewUsableCard = true;
				gs.drawnUsableCard = cardsDrawn.get(0);
			}
		}
	}

	@Override
	public void doEventClient(ClientState gs) {
		if (this.isYou) {
			for (Card c : cardsDrawn) {
				gs.hand.add(c);
			}
		}
	}
}
