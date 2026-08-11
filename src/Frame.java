import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;

public class Frame extends JFrame {

    public Frame() {

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int frameWidth = (int) (screenSize.getWidth() * 0.75);
        int frameHeight = (int) (screenSize.getHeight() * 0.75);

        this.setSize(frameWidth, frameHeight);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        this.add(new Panel(frameWidth, frameHeight));

        setVisible(true);
    }
}