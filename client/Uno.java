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
public class Uno extends JFrame {

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
				name = controlName(name);

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

						String[] userCards = gs.hand.printHand(sortingEnabled).split("\n");

						setImageList(userCards);

						do {
							String in = "";
							// do {
							in = JOptionPane.showInputDialog( 
									"Enter number to play card, 'p' to pick up, 's' to toggle sorting: ");
							// } while (!in.equalsIgnoreCase("s") || !in.equalsIgnoreCase("p"));

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
										validInput = false;
									} else {
										if (c.canSetColor()) {
											boolean validColorInput = false;
											do {
												try {
													setInfoTextArea("Select new color:");
													int color = 1;
													for (Color col : allColors) {
														setInfoTextArea(color + ": " + col.name);
														color++;
													}

													String colorIn = JOptionPane.showInputDialog("Your selection: ");

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
										.showInputDialog("You drew a " + ((PlayerTurnAfterDraw) e).card.makeString()
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
										setInfoTextArea("Select new color:");
										int color = 1;
										for (Color col : allColors) {
											setInfoTextArea(color + ": " + col.name);
											color++;
										}

										String colorIn = JOptionPane.showInputDialog("Your selection: ");
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
						ynInput = JOptionPane.showInputDialog("Play again? [y/n]: ");
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

	private static String controlName(String name) {
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
	public Uno() throws UnknownHostException, IOException {
		setTitle("Uno");
		setIconImage(Toolkit.getDefaultToolkit().getImage(UnoClientGui.class.getResource("/image/unoIcon.jpg")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1033, 700);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnPlayUno = new JMenu("Play Uno");
		menuBar.add(mnPlayUno);

		JMenuItem mntmGameInformation = new JMenuItem("Game Information");
		mntmGameInformation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "INFORMATION\n");
			}
		});
		mnPlayUno.add(mntmGameInformation);

		JMenuItem mntmHowToPlay = new JMenuItem("How to Play");
		mntmHowToPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "How to Play\n");
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
		contentPane.setBackground(new java.awt.Color(0, 0, 205));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		lblTopCardImage = new JLabel("");
		lblTopCardImage.setPreferredSize(new Dimension(390, 546));
		lblTopCardImage.setIcon(new ImageIcon(UnoClientGui.class.getResource("/image/card_back.png")));

		JLabel lblTopCardLabel = new JLabel("TOP CARD");
		lblTopCardLabel.setFont(new Font("Skia", Font.BOLD, 12));
		lblTopCardLabel.setHorizontalAlignment(SwingConstants.CENTER);

		panelForPlayerCards = new JPanel();
		panelForPlayerCards.setBackground(new java.awt.Color(192, 192, 192));

		JPanel textAreaPanel = new JPanel();

		JLabel lblTopDeck = new JLabel("TOP DECK");
		lblTopDeck.setHorizontalAlignment(SwingConstants.CENTER);
		lblTopDeck.setFont(new Font("Skia", Font.BOLD, 12));

		JLabel lblTopDeckImage = new JLabel("");
		lblTopDeckImage.setIcon(new ImageIcon(UnoClientGui.class.getResource("/image/card_back_alt.png")));
		lblTopDeckImage.setPreferredSize(new Dimension(390, 546));
				textAreaPanel.setLayout(new BorderLayout(0, 0));
				
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(UnoClientGui.class.getResource("/image/images.jpeg")));
		
		JLabel lblNewLabel_1 = new JLabel("");
		lblNewLabel_1.setIcon(new ImageIcon(UnoClientGui.class.getResource("/image/deck-uno-game-cards-scattered-260nw-691543729.jpg")));
		
		JLabel lblUserCards = new JLabel("USER CARDS");
		lblUserCards.setFont(new Font("Skia", Font.BOLD, 16));

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		panelForPlayerCards.setLayout(new GridLayout(1, 0, 0, 0));
		textAreaPanel.setLayout(new BorderLayout(0, 0));
		
				textAreaInfo = new JTextArea();
				textAreaInfo.setBackground(new java.awt.Color(192, 192, 192));
				textAreaInfo.setFont(new Font("Skia", Font.ITALIC, 14));
				textAreaInfo.setText("Waiting for other Players to Connect:");
				JScrollPane textAreaJPane = new JScrollPane(textAreaInfo, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				textAreaPanel.add(textAreaJPane);
				gl_contentPane.setHorizontalGroup(
					gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(textAreaPanel, GroupLayout.PREFERRED_SIZE, 595, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_contentPane.createSequentialGroup()
											.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
												.addGroup(gl_contentPane.createSequentialGroup()
													.addGap(33)
													.addComponent(lblTopCardLabel, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE))
												.addComponent(lblTopCardImage, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE))
											.addGap(18)
											.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
												.addComponent(lblTopDeckImage, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
												.addComponent(lblTopDeck, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)))
										.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 284, GroupLayout.PREFERRED_SIZE))
									.addGap(20)
									.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
										.addGroup(gl_contentPane.createSequentialGroup()
											.addComponent(lblUserCards)
											.addGap(93)
											.addComponent(lblNewLabel_1, GroupLayout.PREFERRED_SIZE, 391, GroupLayout.PREFERRED_SIZE))
										.addComponent(textAreaInfo, GroupLayout.PREFERRED_SIZE, 669, GroupLayout.PREFERRED_SIZE)))
								.addComponent(panelForPlayerCards, GroupLayout.PREFERRED_SIZE, 978, GroupLayout.PREFERRED_SIZE))
							.addContainerGap())
				);
				gl_contentPane.setVerticalGroup(
					gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(textAreaInfo, GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
								.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
									.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
										.addComponent(lblTopDeckImage, GroupLayout.PREFERRED_SIZE, 186, GroupLayout.PREFERRED_SIZE)
										.addGroup(gl_contentPane.createSequentialGroup()
											.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
												.addComponent(lblTopCardLabel, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
												.addComponent(lblTopDeck, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(lblTopCardImage, GroupLayout.PREFERRED_SIZE, 179, GroupLayout.PREFERRED_SIZE)))
									.addGap(7)))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
									.addGroup(gl_contentPane.createSequentialGroup()
										.addPreferredGap(ComponentPlacement.RELATED, 156, Short.MAX_VALUE)
										.addComponent(lblUserCards))
									.addComponent(lblNewLabel_1, GroupLayout.PREFERRED_SIZE, 173, Short.MAX_VALUE))
								.addComponent(lblNewLabel, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE))
							.addGap(18)
							.addComponent(panelForPlayerCards, GroupLayout.PREFERRED_SIZE, 219, GroupLayout.PREFERRED_SIZE)
							.addGap(85)
							.addComponent(textAreaPanel, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
				);

		
		
		contentPane.setLayout(gl_contentPane);
	}

	private static void setInfoTextArea(String message) {
		textAreaInfo.setText(textAreaInfo.getText() + "\n" + message);
	}
	
	private static void clear() {
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