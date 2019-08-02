package uno;

import modules.Handler;
import org.json.JSONObject;

public class MessageHandler extends Handler {
    private static final String MODULE = "Uno";

    private String username;

    public MessageHandler() {
        this(null);
    }
    public MessageHandler(String port) {
        super(port);
    }

    @Override
    protected void handle(JSONObject message) {
        if (null == message.optString("module") || !MODULE.equals(message.getString("module"))) {
            return;
        }
        if (null == username) {
            username = message.getString("username");
        }

        if (!message.has("action")) {
            JSONObject errorMessage = new JSONObject();
            errorMessage.put("type", "error");
            errorMessage.put("message", "Skip-Bo messages require an 'action' value");

            netSend(errorMessage, username, MODULE);
            return;
        }

        try {
            switch (message.getString("action")) {
                case "join":
                    JSONObject ackMessage = new JSONObject();
                    ackMessage.put("action", Game.joinGame(this) ? "ack" : "deny");

                    netSend(ackMessage, username, MODULE);

                    break;
                case "drawcard":
                    Game.drawCard(username);

                    break;
                case "send":
                    if (!message.has("card")) {
                        throw new IllegalArgumentException("Must have a card to send");
                    }

                    Game.playCard(new Card(message.getJSONObject("card")), username);
                    break;
                default:
                    System.out.println(message);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            JSONObject errorMessage = new JSONObject();
            errorMessage.put("type", "error");
            errorMessage.put("message", e.getMessage());

            sendToUser(errorMessage);
        }
    }

    public static void main(String[] args) {
        new Thread(new MessageHandler()).start();
    }

    public String getUsername() {
        return username;
    }

    public void sendToUser(JSONObject message) {
        netSend(message, username, MODULE);
    }
}
