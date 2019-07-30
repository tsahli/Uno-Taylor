package uno;

import org.json.JSONObject;

public class Card {
    public static enum Color {
        Red, Yellow, Green, Blue
    }

    private String value;
    private Color color;

    public Card(String value, Color color) {
        this.value = value;
        this.color = color;
    }

    public String getValue() {
        return value;
    }

    public Color getColor() {
        return color;
    }

    public JSONObject toJson() {
        return new JSONObject(this);
    }
}
