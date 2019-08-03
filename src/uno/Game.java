package uno;

import org.json.JSONObject;

import java.util.*;
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

    private Deque<Card> deck;
    private final Queue<Player> players = new ConcurrentLinkedQueue<>();
    private Deque<Card> discard = new ConcurrentLinkedDeque<>();
    private static String currentTurn;

    private Game() {
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
        if (!Objects.equals(currentTurn, player.getUsername())) {
            throw new IllegalArgumentException("It is not player " + username + "'s turn");
        }

        game.discard.add(player.playCard(card));

        JSONObject cardMessage = new JSONObject();
        cardMessage.put("playedCard", card.toJson());

        broadcast(cardMessage);

        game.nextTurn();
    }

    private static void broadcast(JSONObject cardMessage) {
        game.players.forEach(p -> MessageHandler.getInstance().sendToUser(cardMessage, p.getUsername()));
    }

    public static void start() {
        if (null == game || 0 == game.players.size()) {
            throw new IllegalStateException("Unable to start a game - no players have joined");
        }
        if (null != game.deck) {
            throw new IllegalStateException("Game is already started - not starting again");
        }

        final String[] SPECIAL_TYPES = { "Skip", "Draw Two", "Reverse" };

        List<Card> deck = new ArrayList<>();

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
        Collections.shuffle(deck);

        game.deck = new ConcurrentLinkedDeque<>();
        game.deck.addAll(deck);

        MessageHandler handler = MessageHandler.getInstance();
        currentTurn = null;
        game.players.forEach(player -> {
            while (player.handSize() < 7) {
                player.drawCard(game.deck.pop());
            }

            JSONObject message = new JSONObject();
            message.put("initialHand", player.cardArray());

            if (currentTurn == null) {
                currentTurn = player.getUsername();
            }
            handler.sendToUser(message, player.getUsername());
        });

        JSONObject turnMessage = new JSONObject();
        turnMessage.put("turnMessage", "turn");
        handler.sendToUser(turnMessage, currentTurn);
    }

    private void addPlayer(String player) {
        players.add(new Player(player));
    }

    public static void drawCard(String username, boolean updateTurn) {
        if (null == game) {
            game = new Game();
        }

        Player player;
        synchronized (game.players) {
            player = game.players.stream()
                    .filter(p -> p.getUsername().equals(username))
                    .findAny()
                    .orElse(null);
        }

        if (null == player) {
            throw new IllegalStateException("Username " + username + " is not joined");
        }
        if (!Objects.equals(currentTurn, player.getUsername())) {
            throw new IllegalArgumentException("It is not " + username + "'s turn");
        }

        if (null == game.deck) {
            throw new IllegalStateException("Game is not started");
        }
        if (game.deck.size() <= 0) {
            throw new IllegalStateException("Deck is empty");
        }

        Card card = game.deck.pop();
        player.drawCard(card);

        JSONObject cardMessage = new JSONObject();
        cardMessage.put("drawnCard", card.toJson());

        MessageHandler.getInstance().sendToUser(cardMessage, player.getUsername());

        if (updateTurn) {
            game.nextTurn();
        }
    }

    private void nextTurn() {
        synchronized (players) {
            Player nextTurn = players.stream().reduce(null, (turn, player2) -> {
                if (null != turn && Objects.equals(currentTurn, turn.getUsername())) {
                    return player2;
                }
                if (Objects.equals(currentTurn, player2.getUsername())) {
                    return player2;
                }

                return null;
            });

            if (null == nextTurn || Objects.equals(nextTurn.getUsername(), currentTurn)) {
                currentTurn = Objects.requireNonNull(players.peek()).getUsername();
            }
            else {
                currentTurn = nextTurn.getUsername();
            }
        }

        JSONObject turnMessage = new JSONObject();
        turnMessage.put("turnMessage", "turn");
        MessageHandler.getInstance().sendToUser(turnMessage, currentTurn);
    }

    public static synchronized void quit(String username) {
        if (null == game) {
            return;
        }

        boolean playerQuit;
        synchronized(game.players) {
            playerQuit = game.players.removeIf(player -> Objects.equals(username, player.getUsername()));
        }

        if (0 == game.players.size()) {
            game = null;
            return;
        }

        if (playerQuit) {
            JSONObject message = new JSONObject();
            message.put("quit", username);

            broadcast(message);

            message.put("quit", "quit");
            MessageHandler.getInstance().sendToUser(message, username);
        }
        else {
            throw new IllegalArgumentException("User " + username + " isn't in the game");
        }
    }
}
