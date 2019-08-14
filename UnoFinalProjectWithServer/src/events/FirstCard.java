package events;

import cardState.Card;
import state.ClientState;
import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/8/2019
 * @description Event for drawing the first card from the deck
 */
public class FirstCard extends GameEvent {
	private static final long serialVersionUID = 1L;
	public Card card;

	/**
	 * 
	 * @param card
	 */
	public FirstCard(Card card) {
		this.card = card;
	}
	
	/**
	 * 
	 */
	@Override
	public void subMakePrivate() {
	}

	/**
	 * 
	 */
	@Override
	public String makeString() {
		String output = "The first card on the pile is " + card.toString();
		return output;
	}

	/**
	 * 
	 */
	@Override
	public GameEvent clone() {
		FirstCard e = new FirstCard(card);
		copyParentAttrs(e);
		return e;
	}

	/**
	 * 
	 */
	@Override
	public void doEventServer(ServerState gs) {
	}

	/**
	 * 
	 */
	@Override
	public void doEventClient(ClientState gs) {
		gs.topCard = card;
	}
}
