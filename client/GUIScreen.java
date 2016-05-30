import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.text.DefaultCaret;

public class GUIScreen extends JFrame implements MouseListener, KeyListener {
	private static final long serialVersionUID = -1289212737431415180L;
	private HashMap<String, String> messageScreens;
	private ArrayList<String> screens;
	private JPanel scrleft;
	private String currScreen;
	private UserInterface ui;
	private JTextArea msgList;
	private JTextField typing;
		
	public GUIScreen (UserInterface ui) {
		this.ui = ui;
		this.messageScreens = new HashMap<String, String>();
		this.screens = new ArrayList<String>();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1024, 768);
		setTitle("RSAChat");
		setResizable(false);
		JPanel glob = new JPanel();
		glob.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		scrleft = new JPanel();
		JPanel scrright = new JPanel();
		JScrollPane jsp = new JScrollPane(scrleft);
		jsp.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		jsp.setPreferredSize(new Dimension(256, 768));
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		c.weightx = .25;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.VERTICAL;
		glob.add(jsp, c);
		scrright.setPreferredSize(new Dimension(768, 768));
		scrright.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		c.weightx = .75;
		c.gridx = 1;
		c.gridy = 0;
		glob.add(scrright, c);
		
		scrleft.setLayout(new BoxLayout(scrleft, BoxLayout.Y_AXIS));
		
		UserListItem uli = new UserListItem("Send Message");
		uli.addMouseListener(this);
		scrleft.add(uli);
		// that shit

		scrright.setLayout(new BoxLayout(scrright, BoxLayout.Y_AXIS));
		msgList = new JTextArea();
		msgList.setLineWrap(true);
		msgList.setWrapStyleWord(true);
		msgList.setBackground(Color.WHITE);
		msgList.setFont(msgList.getFont().deriveFont(16.0F));
		
		DefaultCaret car = (DefaultCaret) msgList.getCaret();
		car.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		msgList.setEditable(false);
		msgList.setEnabled(false);
		JScrollPane scrollmsg = new JScrollPane(msgList);
		scrollmsg.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollmsg.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollmsg.setViewportView(msgList);
		scrollmsg.setPreferredSize(new Dimension(768, 718));
		scrright.add(scrollmsg);
		
		scrright.add(Box.createRigidArea(new Dimension(768, 5)));
		
		typing = new JTextField();
		typing.setPreferredSize(new Dimension(740, 23));
		typing.setEnabled(false);
		typing.addKeyListener(this);
		scrright.add(typing);
		add(glob);
	}
	
	private void addMessageUser () {
		String user = JOptionPane.showInputDialog(this, "Who would you like to message?");
		if (user.equals("Send Message")) {
			addMessageUser();
			return;
		}
		createUser(user);
	}
	
	private void createUser (String user) {
		user = user.trim();
		if (!screens.contains(user)) {
			screens.add(user);
			UserListItem uli = new UserListItem(user);
			uli.addMouseListener(this);
			scrleft.add(uli);
			getContentPane().revalidate();
			getContentPane().repaint();
			displayLitMessage(user, "** Chatting With "+user+" **");
		}
	}
	
	private void switchUser (String user) {
		Component[] cmps = scrleft.getComponents();
		for (Component cmp : cmps) {
			if (cmp instanceof UserListItem) {
				if (((UserListItem)cmp).getUName().equals(user)) {
					cmp.setBackground(new Color(224, 224, 224));
				} else {
					cmp.setBackground(Color.WHITE);
				}
			}
		}
		messageScreens.put(currScreen, msgList.getText());
		msgList.setText(messageScreens.get(user));
		msgList.setEnabled(true);
		typing.setEnabled(true);
		currScreen = user;
	}
	
	private void addLine (String ln) {
		if (msgList.getText() != null) {
			if (msgList.getText().length() > 0) {
				msgList.setText(msgList.getText()+"\n"+ln.trim());
				return;
			}
		}
		msgList.setText(ln.trim());
	}
	
	private void displayLitMessage (String screen, String message) {
		createUser(screen);
		if (currScreen.equals(screen)) {
			String currCont = msgList.getText();
			if (currCont == null);
			else if (currCont.length() <= 0);
			else {
				msgList.setText(currCont+"\n"+message);
				return;
			}
			msgList.setText(message);
			switchUser(screen);
			return;
		}
		String currTxt = messageScreens.get(screen);
		if (currTxt == null);
		else if (currTxt.length() <= 0);
		else {
			messageScreens.put(screen, currTxt+"\n"+message);
			return;
		}
		messageScreens.put(screen, message);
		switchUser(screen);		
	}
	
	public void displayMessage (String from, String message) {
		from = from.trim();
		message = message.trim();
		displayLitMessage(from, from+": "+message);
	}

	public void keyTyped(KeyEvent e) {}

	public void keyReleased(KeyEvent e) {}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() != KeyEvent.VK_ENTER) return;
		if (typing.getText().length() <= 0) return;
		ui.sendMessage(currScreen, typing.getText());
		addLine("You: "+typing.getText());
		typing.setText("");
	}
	
	public void mouseClicked(MouseEvent e) {
		if (!(e.getSource() instanceof UserListItem)) return;
		String usescr = ((UserListItem) e.getSource()).getUName();
		if (usescr.equalsIgnoreCase("Send Message")) addMessageUser();
		else switchUser(usescr);
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

}
