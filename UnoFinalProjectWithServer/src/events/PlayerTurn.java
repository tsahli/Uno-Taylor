package events;

import state.ClientState;
import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/8/2019
 * @description Event for handling your turn and also used for the server side as well. 
 */
public class PlayerTurn extends GameEvent {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void subMakePrivate() {
	}

	@Override
	public String makeString() {
		return "YourTurn";
	}

	@Override
	public GameEvent clone() {
		PlayerTurn e = new PlayerTurn();
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