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

        switch (message.getString("action")) {
            case "login":
                JSONObject ackMessage = new JSONObject();
                ackMessage.put("action", Game.joinGame(this) ? "ack" : "deny");

                netSend(ackMessage, username, MODULE);

                break;
            case "draw card":
                Card card = Game.drawCard(username);

                JSONObject cardMessage = new JSONObject();
                cardMessage.put("action", "draw card");
                cardMessage.put("card", card.toJson());

                netSend(cardMessage, username, MODULE);

                break;
            default:
                System.out.println(message);
        }
    }

    public static void main(String[] args) {
        new Thread(new MessageHandler()).start();
    }

    public String getUsername() {
        return username;
    }
}
