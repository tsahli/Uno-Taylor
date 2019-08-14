package client;

import cards.Card;
import cards.Color;
import events.*;
import state.ClientState;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientGUI extends JFrame {
    private static JTextArea textArea = new JTextArea(32, 64);

    public ClientGUI() {
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setMargin(new Insets(2, 2, 2, 2));
        Container container = getContentPane();
        container.add(textArea, BorderLayout.CENTER);
        container.add(new JScrollPane(textArea), BorderLayout.CENTER);
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
        int port = 9886;

        ClientGUI frame;
        frame = new ClientGUI();
        boolean clientRunning = true;
        while (clientRunning) {
            addText("Waiting for all connections...");
            Socket socket = new Socket(hostname, port);

            gameState = new ClientState();
            ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inFromServer = new ObjectInputStream(socket.getInputStream());

            outToServer.writeObject(username);
            GameEvent event;
            boolean gameActive = true;

            while (gameActive) {
                event = (GameEvent) inFromServer.readObject();
                if (event.makeString().equals("YourTurn")) {
                    boolean validInput = false;
                    addText("\nIt's your turn!");
                    addText("Top card is: " + gameState.topCard.makeString());
                    addText("Your hand:");
                    addText(gameState.hand.printHand());

                    do {
                        String response = JOptionPane.showInputDialog("Enter number to play card, 'p' to draw: ");
                        if (response.toLowerCase().equals("p")) {
                            outToServer.writeObject(new DrawCard());
                            validInput = true;
                        } else {
                            try {
                                int number = Integer.valueOf(response).intValue();
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
                                }
                            } catch (NumberFormatException e) {
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
}
