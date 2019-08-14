package events;

import state.ClientState;
import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/8/2019
 * @description Handles the null values sent. Checks if the user has sent a null value in its actions
 */
public class NullEvent extends GameEvent {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@Override
	public void subMakePrivate() {
	}

	@Override
	public GameEvent clone() {
		NullEvent e = new NullEvent();
		copyParentAttrs(e);
		return e;
	}

	@Override
	public String makeString() {
		return "NullEvent";
	}

	@Override
	public void doEventServer(ServerState gs) {
	}

	@Override
	public void doEventClient(ClientState gs) {
	}
}
