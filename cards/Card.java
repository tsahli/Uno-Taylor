package cards;

import java.io.Serializable;

import state.ServerState;

public abstract class Card implements Serializable, Comparable<Card> {
	private static final long serialVersionUID = 1L;
	private static int nextUniqueId = 0;
	
	public static final int NUMBER = 0;
	public static final int SKIP = 1;
	public static final int REVERSE = 2;
	public static final int DRAWTWO = 3;
	public static final int WILD = 4;
	public static final int WILDDRAWFOUR = 5;
	
	public Color color;
	public int type;
	public int uniqueId;
	
	public Card(int type) {
		uniqueId = nextUniqueId;
		nextUniqueId++;
		this.type = type;
	}
	
	public abstract void doCardAction(ServerState gs);
	
	public abstract boolean canSetColor();
	
	public abstract String makeString();
	
	public String toString() {
		return makeString();
	}
	
	public boolean equals(Object o) {
		Card c = (Card) o;
		return (c.uniqueId == this.uniqueId);
	}
	
	public int compareTo(Card o) {
		if (this.equals(o))
			return 0;
		if (this.color.color == o.color.color) {
			if (this.type == o.type) {
				if (this.type == NUMBER) {
					NumberCard c1 = (NumberCard) this;
					NumberCard c2 = (NumberCard) o;
					return c1.number - c2.number;
				} else {
					return 0;
				}
			} else {
				return this.type - o.type;
			}
		} else {
			return this.color.color - o.color.color;
		}
	}
	
	// Can this card be placed on card c?
	public boolean canPlaceOn(Card c) {
		if (this.color.color == c.color.color)
			return true;
		if (this.type == c.type)
			return true;
		return false;
	}
}