package events;

import state.ClientState;
import state.ServerState;

public class NullEvent extends GameEvent {
	private static final long serialVersionUID = 1L;
	
	public NullEvent() {
	}

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
