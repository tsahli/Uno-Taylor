package client;

import cards.Card;
import cards.Color;
import events.*;
import state.ClientState;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientGUI extends JFrame {
    private static JTextArea textArea = new JTextArea(32, 64);
    private static JButton drawButton = new JButton("Draw Card");
    private static JButton playCardButton = new JButton("Play a Card");
    private static String hostname;
    private static int port = 9886;
    private static Socket socket;
    private static boolean validInput = false;
    private static String cardNumber;

    static {
        try {
            socket = new Socket(hostname, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ObjectOutputStream outToServer;

    static {
        try {
            outToServer = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ObjectInputStream inFromServer;

    static {
        try {
            inFromServer = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public ClientGUI() throws IOException {
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setMargin(new Insets(2, 2, 2, 2));
        drawButton.addActionListener(new drawButtonListener());
        playCardButton.addActionListener(new cardListener());
        Container container = getContentPane();
        container.add(textArea, BorderLayout.NORTH);
        container.add(drawButton, BorderLayout.SOUTH);
        container.add(playCardButton, BorderLayout.CENTER);
        container.add(new JScrollPane(textArea), BorderLayout.NORTH);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        setTitle("UNO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public static void addText(String text) {
        textArea.append(text + "\n");
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ClientState gameState;
        Color[] allColors = new Color[]{Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW};
        String hostname = JOptionPane.showInputDialog("Enter IP of game server: ");
        String username = JOptionPane.showInputDialog("Enter your username: ");

        ClientGUI frame;
        frame = new ClientGUI();
        boolean clientRunning = true;
        while (clientRunning) {
            addText("Waiting for all connections...");

            gameState = new ClientState();


            outToServer.writeObject(username);
            GameEvent event;
            boolean gameActive = true;

            while (gameActive) {
                event = (GameEvent) inFromServer.readObject();
                if (event.makeString().equals("YourTurn")) {
                    //validInput = false;
                    addText("\nIt's your turn!");
                    addText("Top card is: " + gameState.topCard.makeString());
                    addText("Your hand:");
                    addText(gameState.hand.printHand());

                    do {
                        //String response = JOptionPane.showInputDialog("Enter number to play card, 'p' to draw: ");
                        if (drawButton.getModel().isPressed()) {
                            addText("You drew a card!");
                        } else if (cardNumber != "") {
                            try {
                                //String cardNumber = JOptionPane.showInputDialog("Enter the number of the card you want to play: ");
                                int number = Integer.valueOf(cardNumber).intValue();
                                Card card = gameState.hand.cards.get(number - 1);
                                if (!card.canPlaceOn(gameState.topCard)) {
                                    addText("You can't play that card!");
                                    validInput = false;
                                } else {
                                    if (card.canSetColor()) {
                                        boolean validColorInput = false;
                                        do {
                                            try {
                                                addText("Select a new color, by number: ");
                                                int color = 1;
                                                for (Color col : allColors) {
                                                    addText(color + ": " + col.name);
                                                    color++;
                                                }
                                                String colorChoice = JOptionPane.showInputDialog("Choose a color: ");
                                                int colorChoiceNumber = Integer.valueOf(colorChoice).intValue();
                                                if (colorChoiceNumber > 0 && colorChoiceNumber < 5) {
                                                    card.color = allColors[colorChoiceNumber - 1];
                                                    validColorInput = true;
                                                } else {
                                                    validColorInput = false;
                                                }
                                            } catch (NumberFormatException e) {
                                                validColorInput = false;
                                            }
                                        } while (!validColorInput);
                                    }
                                    outToServer.writeObject(new PlaceCard(card));
                                    validInput = true;
                                    cardNumber = "";
                                }
                            } catch (NumberFormatException e) {
                                cardNumber = "";
                                validInput = false;
                            } catch (IndexOutOfBoundsException e) {
                                validInput = false;
                            }
                        }

                    } while (!validInput);
                } else if (event.makeString().equals("YourTurnAfterDraw")) {
                    boolean validTurnAfterDraw = false;
                    boolean playCard = false;
                    do {
                        String ynInput = JOptionPane.showInputDialog("You drew a " + ((YourTurnAfterDraw) event).card.makeString() + ", which can be played. Play this card? [y/n]: ");
                        if (ynInput.toLowerCase().equals("y")) {
                            playCard = true;
                            validTurnAfterDraw = true;
                        } else if (ynInput.toLowerCase().equals("n")) {
                            playCard = false;
                            validTurnAfterDraw = true;
                        } else {
                            validTurnAfterDraw = false;
                        }
                    } while (!validTurnAfterDraw);
                    if (playCard) {
                        Card c = ((YourTurnAfterDraw) event).card;
                        if (c.canSetColor()) {
                            boolean validColorInput = false;
                            do {
                                try {
                                    addText("Select new color:");
                                    int color = 1;
                                    for (Color col : allColors) {
                                        addText(color + ": " + col.name);
                                        color++;
                                    }
                                    String colorIn = JOptionPane.showInputDialog("Your selection: ");
                                    int colorInNum = Integer.valueOf(colorIn).intValue();
                                    if (colorInNum > 0 && colorInNum < 5) {
                                        c.color = allColors[colorInNum - 1];
                                        validColorInput = true;
                                    } else {
                                        validColorInput = false;
                                    }
                                } catch (NumberFormatException e1) {
                                    validColorInput = false;
                                }
                            } while (!validColorInput);
                        }
                        outToServer.writeObject(new PlaceCard(c));
                    } else {
                        outToServer.writeObject(new NullEvent());
                    }
                } else {
                    event.doEventClient(gameState);
                    addText(event.makeString());
                }
                if (gameState.gameEnded)
                    gameActive = false;
            }
            socket.close();

            boolean validPlayAgainInput = false;
            do {
                String ynInput = JOptionPane.showInputDialog("Play again? [y/n]: ");
                if (ynInput.toLowerCase().equals("y")) {
                    validPlayAgainInput = true;
                } else if (ynInput.toLowerCase().equals("n")) {
                    clientRunning = false;
                    validPlayAgainInput = true;
                } else {
                    validPlayAgainInput = false;
                }
            } while (!validPlayAgainInput);
        }
    }

    private class drawButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                outToServer.writeObject(new DrawCard());
                validInput = true;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private class cardListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            cardNumber = JOptionPane.showInputDialog("Enter number of card you want to play: ");
        }
    }
}
