package cards;

import state.ServerState;

public class ReverseCard extends ActionCard {
	private static final long serialVersionUID = 1L;
	
	public ReverseCard(Color color) {
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
