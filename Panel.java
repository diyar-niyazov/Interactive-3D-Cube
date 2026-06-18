import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.Arrays;
import javax.swing.JPanel;

public class Panel extends JPanel {
    private final double SIDE_LENGTH;
    private final int WIDTH, HEIGHT, Z_NEAR;
    private double[][] vertices;
    private double[][] projectedVertices;
    private int[][] translatedVertices;

    public Panel() {
        setBackground(Color.BLACK);

        SIDE_LENGTH = 2;

        WIDTH = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2;
        System.out.println("WIDTH: " + WIDTH);
        HEIGHT = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2;
        System.out.println("HEIGHT: " + HEIGHT);
        System.out.println();
        Z_NEAR = 2;

        vertices = new double[][] {
                // Front Bottom Left
                { -SIDE_LENGTH, -SIDE_LENGTH, 0 },
                // Front Bottom Right
                { SIDE_LENGTH, -SIDE_LENGTH, 0 },
                // Front Top Right
                { SIDE_LENGTH, SIDE_LENGTH, 0 },
                // Front Top Left
                { -SIDE_LENGTH, SIDE_LENGTH, 0 },
                // Back Bottom Left
                { -SIDE_LENGTH, -SIDE_LENGTH, SIDE_LENGTH },
                // Back Bottom Right
                { SIDE_LENGTH, -SIDE_LENGTH, SIDE_LENGTH },
                // Back Top Right
                { SIDE_LENGTH, SIDE_LENGTH, SIDE_LENGTH },
                // Back Top Left
                { -SIDE_LENGTH, SIDE_LENGTH, SIDE_LENGTH } };
        System.out.println("VERTICES");
        print(vertices);
        projectedVertices = project(vertices);
        System.out.println("\nPROJECTED VERTICES");
        print(projectedVertices);
        translatedVertices = translate(projectedVertices);
        System.out.println("\nTRANSLATED VERTICES");
        print(translatedVertices);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);

        drawPoints(g, translatedVertices);
        drawLines(g, translatedVertices);
    }

    public void drawPoints(Graphics g, int[][] vertices) {
        for (int[] coordinate : vertices) {
            int width = 10;
            g.fillRect(
                    coordinate[0] - width / 2,
                    coordinate[1] - width / 2,
                    width,
                    width);
        }
    }

    public void drawLines(Graphics g, int[][] coordinates) {
        for (int i = 0; i < 4; i++) {
            g.drawLine(coordinates[i][0], coordinates[i][1],
                    coordinates[(i + 1) % 4][0],
                    coordinates[(i + 1) % 4][1]);
        }
        for (int i = 0; i < 4; i++) {
            g.drawLine(coordinates[i][0], coordinates[i][1],
                    coordinates[(i + 1) % coordinates.length + 3][0],
                    coordinates[(i + 1) % coordinates.length + 3][1]);
        }
        for (int i = 0; i < 3; i++) {
            g.drawLine(coordinates[i + 4][0], coordinates[i + 4][1],
                    coordinates[(i + 5) % coordinates.length][0],
                    coordinates[(i + 5) % coordinates.length][1]);
        }
        g.drawLine(coordinates[7][0],
                coordinates[7][1],
                coordinates[4][0],
                coordinates[4][1]);
    }

    public double[][] project(double[][] vertices) {
        double[][] result = new double[vertices.length][2];
        for (int i = 0; i < vertices.length; i++) {
            result[i][0] = vertices[i][0] / (vertices[i][2] + Z_NEAR);
            result[i][1] = vertices[i][1] / (vertices[i][2] + Z_NEAR);
        }
        return result;
    }

    public int[][] translate(double[][] coordinates) {
        // -1..1 => 0..2 => 0..2w => 0..w
        int[][] result = new int[vertices.length][2];
        for (int i = 0; i < coordinates.length; i++) {
            result[i][0] = (int) ((coordinates[i][0] + SIDE_LENGTH) * WIDTH / 2);

            System.out.println("OLD X: " + coordinates[i][0]);
            System.out.println("NEW X: " + (int) ((coordinates[i][0] + SIDE_LENGTH) * WIDTH / 2));

            result[i][1] = (int) ((-1 * coordinates[i][1] + SIDE_LENGTH) * HEIGHT / 2);

            System.out.println("OLD Y: " + coordinates[i][1]);
            System.out.println("NEW Y: " + (int) ((-1 * coordinates[i][1] + 1) * HEIGHT / 2));

            System.out.println();
        }
        return result;
    }

    public void print(int[][] array) {
        for (int[] row : array) {
            System.out.println(Arrays.toString(row));
        }
    }

    public void print(double[][] array) {
        for (double[] row : array) {
            System.out.println(Arrays.toString(row));
        }
    }
}
