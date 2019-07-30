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
        final String[] SPECIAL_TYPES = { "Skip", "Draw Two", "Reverse" };

        deck.add(new Card("0", Card.Color.Blue));
        deck.add(new Card("0", Card.Color.Green));
        deck.add(new Card("0", Card.Color.Red));
        deck.add(new Card("0", Card.Color.Yellow));

        for (int i = 0; i < 2; i++) {
            for (int j = 1; j <= 9; j++) {
                deck.add(new Card(j + "", Card.Color.Blue));
                deck.add(new Card(j + "", Card.Color.Green));
                deck.add(new Card(j + "", Card.Color.Red));
                deck.add(new Card(j + "", Card.Color.Yellow));
            }

            for (String s: SPECIAL_TYPES) {
                deck.add(new Card(s, Card.Color.Blue));
                deck.add(new Card(s, Card.Color.Green));
                deck.add(new Card(s, Card.Color.Red));
                deck.add(new Card(s, Card.Color.Yellow));
            }
        }

        for (int i = 0; i < 4; i++) {
            deck.add(new Card("Wild", null));
            deck.add(new Card("Wild Draw Four", null));
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
