package cardValue;

import cardState.Card;
import cardState.Color;
import events.DrawCard;
import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/10/2019
 * @description Wild Draw Four card for special instructions
 */
public class WildDrawFour extends Action {
	private static final long serialVersionUID = 1L;
	
	public WildDrawFour() {
		super(WILDDRAWFOUR);
		this.color = Color.NONE;
	}

	@Override
	public void doCardAction(ServerState gs) {
		// Next player will draw four cards and miss their turn
		gs.eventBuffer.add(new DrawCard(gs.nextPlayer(), 4));
		gs.skipNextPlayer = true;
	}

	@Override
	public boolean canSetColor() {
		return true;
	}

	@Override
	public String makeString() {
		String output;
		if (color.equals(Color.NONE))
			output = "Wild Draw Four";
		else
			output = "Wild Draw Four, chosen color: " + color.name;
		return output;
	}
	
	@Override
	public boolean canPlaceOn(Card c) {
		return true;
	}
}
