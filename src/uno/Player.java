package uno;

public class Player {
    private MessageHandler handler;

    public Player(MessageHandler handler) {
        this.handler = handler;
    }

    public String getUsername() {
        return handler.getUsername();
    }
}
