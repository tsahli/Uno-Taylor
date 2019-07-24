package uno;

import modules.Handler;
import org.json.JSONObject;

public class MessageHandler extends Handler {
    private static final String MODULE = "JavaUno";

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

        System.out.println(message);
    }

    public static void main(String[] args) {
        new Thread(new MessageHandler()).start();
    }
}
