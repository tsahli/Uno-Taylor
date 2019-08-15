package cardValue;

import cardState.Color;
import events.DrawCard;
import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/8/2019
 * @description Special card when the next player draws 2 from the deck unless 
 * 				he has a special card for drawing
 */
public class DrawTwo extends Action {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Sets the drawTwo card with its color.
	 * 
	 * @param color
	 */
	public DrawTwo(Color color) {
		super(DRAWTWO);
		this.color = color;
	}
	
	/**
	 * Next player will draw two cards and miss their turn.
	 */
	@Override
	public void doCardAction(ServerState gs) {
		gs.eventBuffer.add(new DrawCard(gs.nextPlayer(), 2));
		gs.skipNextPlayer = true;
	}

	/**
	 * checks if we can set the color with initial start or special card
	 */
	@Override
	public boolean canSetColor() {
		return false;
	}

	/**
	 * makeString for converting it to String
	 */
	@Override
	public String makeString() {
		return this.color.name + " Draw Two";
	}
}
