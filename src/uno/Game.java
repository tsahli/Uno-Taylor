package uno;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Game {
    private static Game game;

    public static boolean joinGame(MessageHandler player) {
        if (null == game) {
            game = new Game();
        }

        boolean joined = false;
        if (game.players.stream().noneMatch(p -> p.getUsername().equals(player.getUsername()))) {
            joined = true;
        }

        return joined;
    }

    private Queue<Player> players = new ConcurrentLinkedQueue<>();
}
