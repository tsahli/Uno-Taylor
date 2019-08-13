package events;

import java.io.Serializable;

import player.Player;
import state.ClientState;
import state.ServerState;

public abstract class GameEvent implements Serializable {
	private static final long serialVersionUID = 1L;
	public Player player;
	public String playerName;
	public boolean isYou = false;
	
	public void setPlayer (Player p) {
		this.player = p;
		playerName = p.name;
	}
	
	public void makeYou() {
		playerName = "You";
		isYou = true;
	}
	
	public void makePrivate() {
		player = null;
		subMakePrivate();
	}
	
	public void copyParentAttrs(GameEvent e) {
		e.player = this.player;
		e.playerName = this.playerName;
		e.isYou = this.isYou;
	}
	
	public abstract void subMakePrivate();
	
	public abstract GameEvent clone();
	
	public abstract String makeString();
	
	public abstract void doEventServer(ServerState gs);

	public abstract void doEventClient(ClientState gs);

	public String toString() {
		return makeString();
	}
}
