package cardState;

import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/8/2019
 * @description 
 */
public class Number extends Card {
	private static final long serialVersionUID = 1L;
	public int number;
	
	public Number(int number, Color color) {
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
			Number nc = (Number) c;
			return (nc.number == this.number);
		}
	}
}
