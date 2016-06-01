import javax.swing.JOptionPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class GUI extends UserInterface {
	private GUIScreen screen;
	
	public static void main (String[] args) {
		new GUI().run();
	}

	public void run () {
		startRunner("localhost", 4044);
		screen = new GUIScreen(this);
		screen.setVisible(true);
		screen.addWindowListener(new WindowAdapter() {
			public void windowClosing (WindowEvent evt) {
				try {
					cli.endConnect();
					System.exit(0);
				} catch (Exception e) {}
			}
		});	
	}

	public String unamePrompt () {
		return JOptionPane.showInputDialog(screen, "Pick a username:");
	}

	public void displayMessage (String sender, String message) {
		try {
			while (screen == null) Thread.sleep(1000); // wait it out
			screen.displayMessage(sender, message);
		} catch (Exception e) { e.printStackTrace(); }
	}

	public void disconnect () {
		System.exit(0);
	}

	public void readyUp () {
	}

	public void sendMessage(String to, String message) {
		try {
			cli.sendMessage(to, message);
		} catch (Exception e) {
			disconnect();
		}
	}

}
