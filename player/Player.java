package player;

import java.io.Serializable;

public class Player implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String name;
	public Hand hand;
	public boolean finished = false;
	public int finishPosition;
	
	public Player(String name) {
		this.name = name;
		this.hand = new Hand();
		System.out.println("New player: " + name);
	}
}
