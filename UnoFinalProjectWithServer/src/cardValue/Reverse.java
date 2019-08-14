package cardValue;

import cardState.Color;
import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/10/2019
 * @description Reverse card for special instructions
 */
public class Reverse extends Action {
	private static final long serialVersionUID = 1L;
	
	public Reverse(Color color) {
		super(REVERSE);
		this.color = color;
	}
	
	@Override
	public void doCardAction(ServerState gs) {
		// Reverse game direction
		gs.changePlayDirection();
	}

	@Override
	public boolean canSetColor() {
		return false;
	}

	@Override
	public String makeString() {
		return this.color.name + " Reverse";
	}
}
