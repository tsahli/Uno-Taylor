package cardValue;

import cardState.Color;
import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/10/2019
 * @description Skip card for special instructions
 */
public class Skip extends Action {
	private static final long serialVersionUID = 1L;
	
	public Skip(Color color) {
		super(SKIP);
		this.color = color;
	}
	
	@Override
	public void doCardAction(ServerState gs) {
		// Next player will miss their turn
		gs.skipNextPlayer = true;
	}

	@Override
	public boolean canSetColor() {
		return false;
	}

	@Override
	public String makeString() {
		return this.color.name + " Skip";
	}
}
