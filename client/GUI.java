import javax.swing.JOptionPane;


public class GUI extends UserInterface {
	private GUIScreen screen;
	
	public static void main (String[] args) {
		new GUI().run();
	}

	public void run () {
		startRunner("awq.thegt.org", 4044);
		screen = new GUIScreen(this);
		screen.setVisible(true);
	}

	public String unamePrompt () {
		return JOptionPane.showInputDialog(screen, "Pick a username:");
	}

	public void displayMessage (String sender, String message) {
		System.out.println(sender+": "+message);
		screen.displayMessage(sender, message);
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
