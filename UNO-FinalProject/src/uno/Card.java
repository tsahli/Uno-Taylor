package uno;

import org.json.JSONObject;

import java.util.Objects;

public class Card {
    public enum Color {
        Red, Yellow, Green, Blue
    }

    private String value;
    private Color color;

    public Card(String value, Color color) {
        this.value = value;
        this.color = color;
    }

    public Card(JSONObject card) {
        if (!card.has("value")) {
            throw new IllegalArgumentException("Card must have a value");
        }

        value = card.optString("value");

        if (!card.has("color")) {
            if (!"Wild".equals(value) && !"Wild Draw Four".equals(value)) {
                throw new IllegalArgumentException("Only a wild card can have no color");
            }
        }
        else {
            switch (card.getString("color")) {
                case "Blue":
                    color = Color.Blue;

                    break;
                case "Green":
                    color = Color.Green;

                    break;
                case "Red":
                    color = Color.Red;

                    break;
                case "Yellow":
                    color = Color.Yellow;

                    break;
                default:
                    throw new IllegalArgumentException("Blue, Green, Red and Yellow are the only allowable colors");
            }
        }
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

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }

        Card card = (Card) otherObject;
        return Objects.equals(value, card.value) &&
                color.equals(card.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, color);
    }

    @Override
    public String toString() {
        return new JSONObject(this).toString();
    }
}