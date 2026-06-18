import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Panel extends JPanel {
    private final double SIDE_LENGTH;
    private final int WIDTH, HEIGHT;
    private int Z_NEAR;
    private double angle = 0;
    private double[][] vertices;
    private double[][] projectedVertices;
    private int[][] translatedVertices;

    public Panel(int WIDTH, int HEIGHT) {
        setBackground(Color.BLACK);

        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        SIDE_LENGTH = 1;
        Z_NEAR = 3;

        vertices = new double[][] {
                // Front Bottom Left
                { -SIDE_LENGTH, -SIDE_LENGTH, -SIDE_LENGTH },
                // Front Bottom Right
                { SIDE_LENGTH, -SIDE_LENGTH, -SIDE_LENGTH },
                // Front Top Right
                { SIDE_LENGTH, SIDE_LENGTH, -SIDE_LENGTH },
                // Front Top Left
                { -SIDE_LENGTH, SIDE_LENGTH, -SIDE_LENGTH },
                // Back Bottom Left
                { -SIDE_LENGTH, -SIDE_LENGTH, SIDE_LENGTH },
                // Back Bottom Right
                { SIDE_LENGTH, -SIDE_LENGTH, SIDE_LENGTH },
                // Back Top Right
                { SIDE_LENGTH, SIDE_LENGTH, SIDE_LENGTH },
                // Back Top Left
                { -SIDE_LENGTH, SIDE_LENGTH, SIDE_LENGTH } };

        Timer timer = new Timer(16, e -> {
            angle += 0.0001;
            vertices = rotateY(vertices, angle);
            projectedVertices = project(vertices);
            translatedVertices = translate(projectedVertices);
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // g.setColor(Color.WHITE);
        // drawPoints(g, translatedVertices);
        g.setColor(Color.GREEN);
        drawLines(g, translatedVertices);
    }

    public void drawPoints(Graphics g, int[][] vertices) {
        for (int[] coordinate : vertices) {
            int width = 4;
            g.fillRect(
                    coordinate[0] - width / 2,
                    coordinate[1] - width / 2,
                    width,
                    width);
        }
    }

    public void drawLines(Graphics g, int[][] c) {

        int[][] edges = {
                { 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 0 },
                { 4, 5 }, { 5, 6 }, { 6, 7 }, { 7, 4 },
                { 0, 4 }, { 1, 5 }, { 2, 6 }, { 3, 7 }
        };
        for (int[] e : edges) {
            g.drawLine(
                    c[e[0]][0],
                    c[e[0]][1],
                    c[e[1]][0],
                    c[e[1]][1]);

        }
    }

    public double[][] rotateY(double[][] v, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double[][] result = new double[v.length][3];

        for (int i = 0; i < v.length; i++) {
            double x = v[i][0];
            double y = v[i][1];
            double z = v[i][2];

            result[i][0] = x * cos + z * sin;
            result[i][1] = y;
            result[i][2] = -x * sin + z * cos;
        }
        return result;
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

            // System.out.println("OLD X: " + coordinates[i][0]);
            // System.out.println("NEW X: " + (int) ((coordinates[i][0] + SIDE_LENGTH) *
            // WIDTH / 2));

            result[i][1] = (int) ((-1 * coordinates[i][1] + SIDE_LENGTH) * HEIGHT / 2);

            // System.out.println("OLD Y: " + coordinates[i][1]);
            // System.out.println("NEW Y: " + (int) ((-1 * coordinates[i][1] + 1) * HEIGHT /
            // 2));

            // System.out.println();
        }
        return result;
    }

    public void printVertices() {
        System.out.println("VERTICES");
        print(vertices);
        projectedVertices = project(vertices);
        System.out.println("\nPROJECTED VERTICES");
        print(projectedVertices);
        translatedVertices = translate(projectedVertices);
        System.out.println("\nTRANSLATED VERTICES");
        print(translatedVertices);
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
