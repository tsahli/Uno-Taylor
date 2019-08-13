package events;

import state.ClientState;
import state.ServerState;

public class YourTurn extends GameEvent {
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
		YourTurn e = new YourTurn();
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
