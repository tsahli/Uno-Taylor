package uno;

import org.json.JSONObject;

public class Card {
    public static enum Color {
        Red, Yellow, Green, Blue
    }

    private int value;
    private Color color;

    public Card(int value, Color color) {
        this.value = value;
        this.color = color;
    }

    public int getValue() {
        return value;
    }

    public Color getColor() {
        return color;
    }

    public JSONObject toJson() {
        return new JSONObject(this);
    }
}
