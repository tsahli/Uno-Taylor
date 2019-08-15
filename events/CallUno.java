package events;

import player.Player;
import state.ClientState;
import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/8/2019
 * @description Handling of the Uno call in both server and client side.
 */
public class CallUno extends GameEvent {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	public CallUno() {
	}
	
	/**
	 * 
	 * @param p
	 */
	public CallUno(Player p) {
		setPlayer(p);
	}

	/**
	 * 
	 */
	@Override
	public void subMakePrivate() {
	}

	
	@Override
	public String makeString() {
		String output;
		if (isYou)
			output = "You shout, \"UNO!\"";
		else
			output = playerName + " shouts, \"UNO!\"";
		return output;
	}

	@Override
	public GameEvent clone() {
		CallUno e = new CallUno();
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
