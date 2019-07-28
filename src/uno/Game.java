package uno;

import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Game {
    private static Game game;

    public static boolean joinGame(MessageHandler player) {
        if (null == game) {
            game = new Game();
        }

        boolean joined = false;
        if (game.players.stream().noneMatch(p -> p.getUsername().equals(player.getUsername()))) {
            game.addPlayer(player);
            joined = true;
        }

        return joined;
    }

    private Deque<Card> deck = new ConcurrentLinkedDeque<>();
    private Queue<Player> players = new ConcurrentLinkedQueue<>();

    private Game() {
        for (int i = 0; i <= 9; i++) {
            deck.add(new Card(i, Card.Color.Blue));
            deck.add(new Card(i, Card.Color.Green));
            deck.add(new Card(i, Card.Color.Red));
            deck.add(new Card(i, Card.Color.Yellow));
        }
    }

    private void addPlayer(MessageHandler player) {
        players.add(new Player(player));
    }

    public static Card drawCard(String username) {
        if (null == game) {
            game = new Game();
        }

        if (game.players.stream().noneMatch(p -> p.getUsername().equals(username))) {
            throw new IllegalStateException("Username " + username + " is not joined");
        }

        return game.deck.pop();
    }
}
