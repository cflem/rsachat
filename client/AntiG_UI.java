import java.io.IOException;

import javax.swing.JOptionPane;

public class AntiG_UI extends UserInterface {
	private Thread runner;
	
	public static void main (String[] args) {
		new AntiG_UI().run();
	}
	
	public void displayMessage(String sender, String message) {
		System.out.println("<"+sender+"> "+message);
	}
	
	public void readyUp () {
		System.out.println("*** Connected to server. ***");
	}

	public void disconnect() {
		if (runner != null)
			try {
				runner.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		System.out.println("*** Disconnected from server. ***");
		System.exit(0);
	}
	
	public String unamePrompt () {
		return JOptionPane.showInputDialog("Pick a username:");
	}
	
	public void sendMessage (String to, String message) {
		try {
			cli.sendMessage(to, message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run () {
		try {
			startRunner("127.0.0.1", 4045);
			while (true) {
				String to = JOptionPane.showInputDialog("To?");
				if (to == null) break;
				String msg = JOptionPane.showInputDialog("Say?");
				if (msg == null) break;
				sendMessage(to,  msg);
			}
			System.out.println("** Entering Listen-Only Mode **");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
