package events;

import cardState.Card;
import state.ClientState;
import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/8/2019
 * @description Event for handling your turn after draw four,
 * 				and also used for the server side as well.
 */
public class PlayerTurnAfterDraw extends GameEvent {
	private static final long serialVersionUID = 1L;
	
	public Card card;
	
	public PlayerTurnAfterDraw(Card card) {
		this.card = card;
	}
	
	@Override
	public void subMakePrivate() {
		this.card = null;
	}

	@Override
	public String makeString() {
		return "YourTurnAfterDraw";
	}

	@Override
	public GameEvent clone() {
		PlayerTurnAfterDraw e = new PlayerTurnAfterDraw(this.card);
		copyParentAttrs(e);
		return e;
	}

	@Override
	public void doEventServer(ServerState gs) {
	}

	@Override
	public void doEventClient(ClientState gs) {
	}
}
