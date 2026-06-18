import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;

public class Frame extends JFrame {

    public Frame() {

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screensize = toolkit.getScreenSize();
        int WIDTH = (int) (screensize.getWidth() / 2);
        int HEIGHT = (int) (screensize.getHeight() / 2);

        this.setSize(WIDTH, HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        this.add(new Panel(WIDTH, HEIGHT));

        setVisible(true);
    }
}