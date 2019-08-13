package events;

import cards.Card;
import state.ClientState;
import state.ServerState;

public class YourTurnAfterDraw extends GameEvent {
	private static final long serialVersionUID = 1L;
	
	public Card card;
	
	public YourTurnAfterDraw(Card card) {
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
		YourTurnAfterDraw e = new YourTurnAfterDraw(this.card);
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
