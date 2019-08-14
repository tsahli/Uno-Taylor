package cardValue;

import cardState.Card;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/10/2019
 * @description Action card for special instructions
 */
public abstract class Action extends Card {
	private static final long serialVersionUID = 1L;
	
	public Action(int type) {
		super(type);
	}
}