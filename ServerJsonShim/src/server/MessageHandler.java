package server;

import modules.Handler;
import org.json.JSONObject;

public class MessageHandler extends Handler {
    private static final String MODULE = "Uno";

    public MessageHandler() {
        this(null);
    }
    public MessageHandler(String port) {
        super(port);
    }

    public static MessageHandler getInstance() {
        return handler;
    }

    private static MessageHandler handler = new MessageHandler();

    @Override
    protected void handle(JSONObject message) {
        if (null == message.optString("module") || !MODULE.equals(message.getString("module"))) {
            return;
        }

        String username = message.getString("username");

        if (!message.has("action")) {
            JSONObject errorMessage = new JSONObject();
            errorMessage.put("type", "error");
            errorMessage.put("message", "Skip-Bo messages require an 'action' value");

            netSend(errorMessage, username, MODULE);
            return;
        }

        try {
            switch (message.getString("action")) {
                case "start":
                    JSONObject ackMessage = new JSONObject();
                    ackMessage.put("action", Game.joinGame(username) ? "ack" : "deny");

                    netSend(ackMessage, username, MODULE);

                    break;
                case "play":
                    Game.start();

                    break;
                case "drawcard":
                    Game.drawCard(username, true);

                    break;
                case "send":
                    if (!message.has("card")) {
                        throw new IllegalArgumentException("Must have a card to send");
                    }

                    Game.playCard(new Card(message.getJSONObject("card")), username);
                    break;
                case "unocall":
                    Game.callUno(username);

                    break;
                case "quit":
                    Game.quit(username);

                    break;
                default:
                    System.out.println(message);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            JSONObject errorMessage = new JSONObject();
            errorMessage.put("type", "error");
            errorMessage.put("message", e.getMessage());

            sendToUser(errorMessage, username);
        }
    }

    public static void main(String[] args) {
        MessageHandler handler = MessageHandler.getInstance();
        new Thread(handler).start();
    }

    public void sendToUser(JSONObject message, String username) {
        netSend(message, username, MODULE);
    }
}