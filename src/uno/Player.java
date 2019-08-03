package uno;

import org.json.JSONObject;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Player {
    private final String username;
    private final Deque<Card> hand = new ConcurrentLinkedDeque<>();

    public Player(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public Card playCard(Card card) {
        Card toPlay = null;
        synchronized (hand) {
            Iterator<Card> iter = hand.iterator();

            while (iter.hasNext()) {
                Card next = iter.next();
                if (next.equals(card)) {
                    toPlay = card;
                    iter.remove();

                    break;
                }
            }
        }

        if (null == toPlay) {
            throw new IllegalArgumentException("Player " + getUsername() + " does not have card " + card + " in hand");
        }

        return toPlay;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("name", getUsername());
        json.put("inHand", hand.size());

        return json;
    }

    public void drawCard(Card card) {
        hand.add(card);
    }
}
