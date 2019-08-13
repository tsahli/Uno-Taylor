package cards;

import state.ServerState;

public class SkipCard extends ActionCard {
	private static final long serialVersionUID = 1L;
	
	public SkipCard(Color color) {
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
