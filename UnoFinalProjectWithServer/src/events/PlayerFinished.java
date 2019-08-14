package events;

import player.Player;
import state.ClientState;
import state.ServerState;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/8/2019
 * @description Event for checking if the game is finished
 */
public class PlayerFinished extends GameEvent {
	private static final long serialVersionUID = 1L;
	
	public PlayerFinished() {
	}
	
	public PlayerFinished(Player p) {
		setPlayer(p);
	}

	@Override
	public void subMakePrivate() {
	}

	@Override
	public String makeString() {
		String output;
		if (isYou)
			output = "You have finished! :-)";
		else
			output = playerName + " has finished! :-)";
		return output;
	}

	@Override
	public GameEvent clone() {
		PlayerFinished e = new PlayerFinished();
		copyParentAttrs(e);
		return e;
	}

	@Override
	public void doEventServer(ServerState gs) {
		player.finished = true;
		player.finishPosition = gs.nextFinishPosition;
		gs.nextFinishPosition++;
	}

	@Override
	public void doEventClient(ClientState gs) {
	}
}
