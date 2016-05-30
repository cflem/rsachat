import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class UserListItem extends JPanel {
	private static final long serialVersionUID = -8370314419666381607L;
	private Dimension size;
	private String uname;
	
	public UserListItem (String userText) {
		size = new Dimension(250, 64);
		uname = userText;
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
		setBackground(Color.WHITE);
		JLabel txt = new JLabel(userText);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		txt.setAlignmentX(CENTER_ALIGNMENT);
		txt.setFont(getFont().deriveFont(24.0F));
		add(Box.createRigidArea(new Dimension(250, 15)));
		add(txt);
	}

	public Dimension getPreferredSize () {
		return size;
	}
	
	public Dimension getMaximumSize () {
		return size;
	}
	
	public Dimension getMinimumSize () {
		return size;
	}
	
	public Dimension getSize () {
		return size;
	}
	
	public String getUName () {
		return uname;
	}
	
	public String toString () {
		return "UserListItem: "+uname;
	}
	
}
