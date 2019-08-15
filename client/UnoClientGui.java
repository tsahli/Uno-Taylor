package client;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import cardState.Card;
import cardState.Color;
import events.DrawCard;
import events.GameEvent;
import events.NullEvent;
import events.PlaceCard;
import events.PlayerTurnAfterDraw;
import state.ClientState;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.awt.Dimension;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * @author Team Uno
 * @class CIS 3230
 * @date 8/8/2019
 * @description Uno Client needs the port(9886) and the host name to connect.
 *              The server lets 2 to 10 number of players.
 */
public class UnoClientGui extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private static JTextArea textAreaInfo;
	private static JPanel panelForPlayerCards;
	private static JLabel lblTopCardImage;

	/**
	 * Launch the application.
	 * 
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public static void main(String[] args) {

		ClientState gs;
		Color[] allColors = new Color[] { Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW };
		boolean sortingEnabled = false;

		String hostname = JOptionPane.showInputDialog("Please enter IP Address of Uno Game Server:");
		int port = 9886;

		UnoClientGui frame;
		try {
			frame = new UnoClientGui();
			frame.setVisible(true);
		} catch (UnknownHostException e3) {
			e3.printStackTrace();
		} catch (IOException e3) {
			e3.printStackTrace();
		}

		String name = "";
		boolean clientRunning = true;
		while (clientRunning) {
			try {
				name = getNameFromUser(name);

				Socket s = new Socket(hostname, port);

				gs = new ClientState();
				ObjectOutputStream outToServer = new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream inFromServer = new ObjectInputStream(s.getInputStream());

				outToServer.writeObject(name);
				setInfoTextArea("All Connected, Now the Game Starts.  \nBest of Luck :-)\n");
				GameEvent e;
				boolean gameActive = true;

				while (gameActive) {
					e = (GameEvent) inFromServer.readObject();
					if (e.makeString().equals("YourTurn")) {
						boolean validInput = false;
						setInfoTextArea("\nIt's your turn!");
						setInfoTextArea("Top card is: " + gs.topCard.makeString());
						setInfoTextArea("Your hand:");

						lblTopCardImage.setIcon(new ImageIcon(UnoClientGui.class
								.getResource("/image/" + gs.topCard.makeString().replaceAll("\\s", "") + ".png")));

						setInfoTextArea(gs.hand.printHand(sortingEnabled));

						clearImageList();
						
						String[] userCards = gs.hand.printHand(sortingEnabled).split("\n");

						setImageList(userCards);

						do {					 
							 String in = JOptionPane.showInputDialog( null,
									 gs.hand.printHand(sortingEnabled) + "\n\nEnter number to play card, 'p' to pick up, 's' to toggle sorting: ");
							
							setInfoTextArea("\n");

							if (in.toLowerCase().equals("s")) {
								if (sortingEnabled) {
									sortingEnabled = false;
									setInfoTextArea("Sorting disabled.");
								} else {
									sortingEnabled = true;
									setInfoTextArea("Sorting enabled.");
									setInfoTextArea(gs.hand.printHand(sortingEnabled));
								}

								validInput = false;
							} else if (in.toLowerCase().equals("p")) {
								outToServer.writeObject(new DrawCard());
								validInput = true;
							} else {
								try {
									int number = Integer.valueOf(in).intValue();
									Card c = gs.hand.cards.get(number - 1);

									if (!c.canPlaceOn(gs.topCard)) {
										setInfoTextArea("You can't place that card!");
										JOptionPane.showMessageDialog(null, "You can't place that card!");
										validInput = false;
									} else {
										if (c.canSetColor()) {
											boolean validColorInput = false;
											do {
												try {
													String option = "Select new color:\n";
													setInfoTextArea("Select new color:");
													int color = 1;
													for (Color col : allColors) {
														setInfoTextArea(color + ": " + col.name);
														option += color + ": " + col.name + "\n";
														color++;
													}

													String colorIn = JOptionPane.showInputDialog(null,option + "\nYour selection: ");

													int colorInNum = Integer.valueOf(colorIn).intValue();
													setInfoTextArea("\n");
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
										validInput = true;
									}
								} catch (NumberFormatException e1) {
									validInput = false;
								} catch (IndexOutOfBoundsException e2) {
									validInput = false;
								}
							}
						} while (!validInput);
					} else if (e.makeString().equals("YourTurnAfterDraw")) {
						boolean validTurnAfterDraw = false;
						boolean playCard = false;
						do {
							String ynInput = "";
							do {
								ynInput = JOptionPane
										.showInputDialog(null,"You drew a " + ((PlayerTurnAfterDraw) e).card.makeString()
												+ ", which can be played. Play this card? [y/n]: ");
							} while (!ynInput.equalsIgnoreCase("y") || !ynInput.equalsIgnoreCase("n"));

							setInfoTextArea("\n");
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
							Card c = ((PlayerTurnAfterDraw) e).card;
							if (c.canSetColor()) {
								boolean validColorInput = false;
								do {
									try {
										String option = "Select new color:\n";
										setInfoTextArea("Select new color:");
										int color = 1;
										for (Color col : allColors) {
											setInfoTextArea(color + ": " + col.name);
											option += color + ": " + col.name + "\n";
											color++;
										}

										String colorIn = JOptionPane.showInputDialog(null,option + "\nYour selection: ");
										int colorInNum = Integer.valueOf(colorIn).intValue();
										System.out.println(" ");
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
						e.doEventClient(gs);
						setInfoTextArea(e.makeString());
					}
					if (gs.gameEnded)
						gameActive = false;
				}
				s.close();

				boolean validPlayAgainInput = false;
				do {
					String ynInput = "";
					do {
						ynInput = JOptionPane.showInputDialog(null,"Play again? [y/n]: ");
					} while (!ynInput.equalsIgnoreCase("y") || !ynInput.equalsIgnoreCase("n"));

					setInfoTextArea("\n");
					if (ynInput.toLowerCase().equals("y")) {
						validPlayAgainInput = true;
					} else if (ynInput.toLowerCase().equals("n")) {
						clientRunning = false;
						validPlayAgainInput = true;
					} else {
						validPlayAgainInput = false;
					}
				} while (!validPlayAgainInput);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
	}

	private static String getNameFromUser(String name) {
		String tmpName = "";

		if (name.equals("")) {
			tmpName = JOptionPane.showInputDialog("Your name: ");
		} else {
			tmpName = JOptionPane.showInputDialog("Your name (hit enter to use \"" + name + "\"): ");
		}

		if (tmpName.equals("")) {
			if (name.equals("")) {
				JOptionPane.showMessageDialog(null, "You did not enter a name!");
				System.exit(-1);
			}
		} else {
			name = tmpName;
		}
		return name;
	}

	/**
	 * Create the frame.
	 */
	public UnoClientGui() throws UnknownHostException, IOException {
		setTitle("Uno");
		setIconImage(Toolkit.getDefaultToolkit().getImage(UnoClientGui.class.getResource("/image/unoIcon.jpg")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1200, 750);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnPlayUno = new JMenu("Play Uno");
		menuBar.add(mnPlayUno);

		JMenuItem mntmGameInformation = new JMenuItem("Game Information");
		mntmGameInformation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "INFORMATION\n\n"
						+ "UNO is the classic card game that's easy to pick up and impossible to put down! \n"
						+ "Players take turns matching a card in their hand with the current card shown on \n"
						+ "top of the deck either by color or number. ... The first player to rid themselves \n"
						+ "of all the cards in their hand before their opponents wins.", "Game Infomation", 
					    JOptionPane.PLAIN_MESSAGE, new ImageIcon("/image/info.gif"));
			}
		});
		mnPlayUno.add(mntmGameInformation);

		JMenuItem mntmHowToPlay = new JMenuItem("How to Play");
		
		mntmHowToPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "How to Play\n"
						+ "This Video might have made it clear, but for more details click this link (https://www.unorules.com/)\n\n"
						+ "Jumping into the Game\r\n" + 
						"Shuffle the cards and deal 7 cards to each player. ...\r\n" + 
						"Put the rest of the Uno cards in the center of the table. ...\r\n" + 
						"Turn over the top card from the draw pile to start the game. ...\r\n" + 
						"Play a card to match the color, number, or symbol on the card. ...\r\n" + 
						"Draw a card from the draw pile if you can't play a card."
						);
				
				//It will play a video automatically
				Media media = new Media("/files/HowToPlayUno.mp3");  
				MediaPlayer mediaPlayer = new MediaPlayer(media);  		
				mediaPlayer.setAutoPlay(true);  
				
			}
		});
		mnPlayUno.add(mntmHowToPlay);

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		mnPlayUno.add(mntmQuit);
		contentPane = new JPanel();
		contentPane.setBackground(java.awt.Color.BLACK);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		lblTopCardImage = new JLabel("");
		lblTopCardImage.setPreferredSize(new Dimension(390, 546));
		lblTopCardImage.setIcon(new ImageIcon(UnoClientGui.class.getResource("/image/card_back.png")));

		JLabel lblTopCardLabel = new JLabel("Top Card:");
		lblTopCardLabel.setForeground(java.awt.Color.WHITE);
		lblTopCardLabel.setFont(new Font("Stencil", Font.BOLD, 20));
		lblTopCardLabel.setHorizontalAlignment(SwingConstants.CENTER);

		panelForPlayerCards = new JPanel();
		panelForPlayerCards.setBackground(java.awt.Color.DARK_GRAY);

		JPanel textAreaPanel = new JPanel();

		JLabel lblTopDeck = new JLabel("Top Deck:");
		lblTopDeck.setForeground(java.awt.Color.WHITE);
		lblTopDeck.setHorizontalAlignment(SwingConstants.CENTER);
		lblTopDeck.setFont(new Font("Stencil", Font.BOLD, 20));

		JLabel lblTopDeckImage = new JLabel("");
		lblTopDeckImage.setIcon(new ImageIcon(UnoClientGui.class.getResource("/image/card_back_alt.png")));
		lblTopDeckImage.setPreferredSize(new Dimension(390, 546));

		JLabel lblUserCards = new JLabel("UserCards");
		lblUserCards.setForeground(java.awt.Color.WHITE);
		lblUserCards.setFont(new Font("Stencil", Font.PLAIN, 15));

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addContainerGap()
							.addComponent(textAreaPanel, GroupLayout.DEFAULT_SIZE, 1256, Short.MAX_VALUE))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(20)
							.addComponent(lblTopCardLabel, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(lblTopCardImage, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
							.addGap(205)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(lblUserCards, GroupLayout.PREFERRED_SIZE, 86, GroupLayout.PREFERRED_SIZE)
									.addGap(261))
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(lblTopDeck, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE)
									.addGap(18)))
							.addComponent(lblTopDeckImage, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
							.addGap(142))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addContainerGap()
							.addComponent(panelForPlayerCards, GroupLayout.DEFAULT_SIZE, 1156, Short.MAX_VALUE)))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
							.addComponent(lblTopCardImage, GroupLayout.PREFERRED_SIZE, 179, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblTopCardLabel, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblTopDeckImage, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblTopDeck, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblUserCards, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panelForPlayerCards, GroupLayout.PREFERRED_SIZE, 263, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(textAreaPanel, GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE))
		);
		panelForPlayerCards.setLayout(new GridLayout(1, 0, 0, 0));
		textAreaPanel.setLayout(new BorderLayout(0, 0));

		textAreaInfo = new JTextArea();
		textAreaInfo.setBackground(java.awt.Color.LIGHT_GRAY);
		textAreaInfo.setText("Waiting for other Players to Connect:");

		JScrollPane textAreaJPane = new JScrollPane(textAreaInfo, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		textAreaJPane.setBackground(java.awt.Color.BLACK);
		textAreaPanel.add(textAreaJPane);
		contentPane.setLayout(gl_contentPane);
	}

	private static void setInfoTextArea(String message) {
		textAreaInfo.setText(textAreaInfo.getText() + "\n" + message);
	}
	
	private static void clearImageList() {
		panelForPlayerCards.removeAll();
	}
	
	private static void setImageList(String[] imageLists) {
		String imageName;
		for (int i = 0; i < imageLists.length; i++) {
			// Remove whiteSpaces
			imageName = imageLists[i].substring(3).replaceAll("\\s", "");

			JLabel labelImage = new JLabel("");
			labelImage.setIcon(new ImageIcon(UnoClientGui.class.getResource("/image/" + imageName + ".png")));
			panelForPlayerCards.add(labelImage);
		}
	}
}