package cardState;

import java.io.Serializable;

import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/10/2019
 * @description Action card for special instructions
 */
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
	
	/**
	 * Initializing a card with its type number based on the finals fields
	 * 
	 * @param type
	 */
	public Card(int type) {
		uniqueId = nextUniqueId;
		nextUniqueId++;
		this.type = type;
	}
	
	/**
	 * Card's instruction to be done after
	 * 
	 * @param gs
	 */
	public abstract void doCardAction(ServerState gs);
	
	/**
	 * checking if we set the color with wild card
	 * 
	 * @return
	 */
	public abstract boolean canSetColor();
	
	/**
	 * Converts a card object to a string
	 * 
	 * @return
	 */
	public abstract String makeString();
	
	/**
	 * uses toString for clarity of conversion
	 */
	public String toString() {
		return makeString();
	}
	
	/**
	 * checks if the the card objects are equal or not
	 */
	public boolean equals(Object o) {
		Card c = (Card) o;
		return (c.uniqueId == this.uniqueId);
	}
	
	/**
	 * 
	 */
	public int compareTo(Card o) {
		if (this.equals(o))
			return 0;
		if (this.color.color == o.color.color) {
			if (this.type == o.type) {
				if (this.type == NUMBER) {
					Number c1 = (Number) this;
					Number c2 = (Number) o;
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
	
	/**
	 *  Can this card be placed on card c?
	 *  
	 * @param c
	 * @return true or false based on the card to be placed
	 */
	public boolean canPlaceOn(Card c) {
		if (this.color.color == c.color.color)
			return true;
		if (this.type == c.type)
			return true;
		return false;
	}
}