package cards;

import state.ServerState;

public class NumberCard extends Card {
	private static final long serialVersionUID = 1L;
	public int number;
	
	public NumberCard(int number, Color color) {
		super(NUMBER);
		this.color = color;
		this.number = number;
	}

	@Override
	public void doCardAction(ServerState gs) {
	}

	@Override
	public boolean canSetColor() {
		return false;
	}

	@Override
	public String makeString() {
		return this.color.name + " " + String.valueOf(this.number);// + " (id " + String.valueOf(uniqueId) + ")";
	}
	
	@Override
	public boolean canPlaceOn(Card c) {
		if (this.color.equals(c.color))
			return true;
		if (c.type != NUMBER)
			return false;
		else {
			NumberCard nc = (NumberCard) c;
			return (nc.number == this.number);
		}
	}
}
