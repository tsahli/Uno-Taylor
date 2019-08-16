package cardValue;

import cardState.Card;
import cardState.Color;
import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/10/2019
 * @description Wild card for special instructions
 */
public class Wild extends Action {
	private static final long serialVersionUID = 1L;
	
	public Wild() {
		super(WILD);
		this.color = Color.NONE;
	}

	@Override
	public void doCardAction(ServerState gs) {
	}

	@Override
	public boolean canSetColor() {
		return true;
	}

	@Override
	public String makeString() {
		String output;
		if (color.equals(Color.NONE))
			output = "Wild";
		else
			output = "Wild, chosen color: " + color.name;
		return output;
	}
	
	@Override
	public boolean canPlaceOn(Card c) {
		return true;
	}
}
