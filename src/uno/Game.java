package uno;

import org.json.JSONObject;

import java.util.Deque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Game {
    private static Game game;

    public static boolean joinGame(String username) {
        if (null == game) {
            game = new Game();
        }

        boolean joined = false;
        if (game.players.stream().noneMatch(p -> p.getUsername().equals(username))) {
            game.addPlayer(username);
            joined = true;
        }

        return joined;
    }

    private Deque<Card> deck = new ConcurrentLinkedDeque<>();
    private final Queue<Player> players = new ConcurrentLinkedQueue<>();
    private Deque<Card> discard = new ConcurrentLinkedDeque<>();

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

    public static void playCard(Card card, String username) {
        if (null == game) {
            throw new IllegalStateException("No game is currently in progress");
        }

        Player player = game.players.stream()
                .filter(p -> p.getUsername().equals(username))
                .findAny()
                .orElse(null);
        if (null == player) {
            throw new IllegalArgumentException("No player with username " + username + " is playing");
        }

        game.discard.add(player.playCard(card));

        JSONObject cardMessage = new JSONObject();
        cardMessage.put("playedCard", card.toJson());

        broadcast(cardMessage);
    }

    private static void broadcast(JSONObject cardMessage) {
        game.players.forEach(p -> MessageHandler.getInstance().sendToUser(cardMessage, p.getUsername()));
    }

    private void addPlayer(String player) {
        players.add(new Player(player));
    }

    public static void drawCard(String username) {
        if (null == game) {
            game = new Game();
        }

        Player player = game.players.stream()
                .filter(p -> p.getUsername().equals(username))
                .findAny()
                .orElse(null);
        if (null == player) {
            throw new IllegalStateException("Username " + username + " is not joined");
        }

        if (game.deck.size() <= 0) {
            throw new IllegalStateException("Deck is empty");
        }

        Card card = game.deck.pop();
        player.drawCard(card);

        JSONObject cardMessage = new JSONObject();
        cardMessage.put("drawnCard", card.toJson());

        MessageHandler.getInstance().sendToUser(cardMessage, player.getUsername());
    }

    public static synchronized void quit(String username) {
        if (null == game) {
            return;
        }

        boolean playerQuit;
        synchronized(game.players) {
            playerQuit = game.players.removeIf(player -> Objects.equals(username, player.getUsername()));
        }

        if (playerQuit) {
            JSONObject message = new JSONObject();
            message.put("action", "quit");
            message.put("username", username);

            broadcast(message);
        }
        else {
            throw new IllegalArgumentException("User " + username + " isn't in the game");
        }
    }
}
