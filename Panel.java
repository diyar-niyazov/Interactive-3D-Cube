import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Panel extends JPanel {
    private static final double CUBE_SIDE_LENGTH = 1;
    // distance from the camera to the projection plane
    private static final int Z_NEAR = 3;
    private static final int FPS = 60;

    private final int screenWidth, screenHeight;

    private double angle = 0;
    private double angleIncrement = Math.PI / FPS / 2;

    private double[][] initialVertices;
    private double[][] rotatedVertices;
    private double[][] projectedVertices;
    private int[][] translatedVertices;

    public Panel(int frameWidth, int frameHeight) {
        setBackground(Color.BLACK);

        this.screenWidth = frameWidth;
        this.screenHeight = frameHeight;

        initialVertices = new double[][] {
                // Front Bottom Left
                { -CUBE_SIDE_LENGTH, -CUBE_SIDE_LENGTH, -CUBE_SIDE_LENGTH },
                // Front Bottom Right
                { CUBE_SIDE_LENGTH, -CUBE_SIDE_LENGTH, -CUBE_SIDE_LENGTH },
                // Front Top Right
                { CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH, -CUBE_SIDE_LENGTH },
                // Front Top Left
                { -CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH, -CUBE_SIDE_LENGTH },
                // Back Bottom Left
                { -CUBE_SIDE_LENGTH, -CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH },
                // Back Bottom Right
                { CUBE_SIDE_LENGTH, -CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH },
                // Back Top Right
                { CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH },
                // Back Top Left
                { -CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH } };

        rotatedVertices = initialVertices;

        Timer timer = new Timer(16, e -> {
            angle += angleIncrement;
            rotatedVertices = rotateY(initialVertices, angle);
            projectedVertices = project(rotatedVertices);
            translatedVertices = translate(projectedVertices);
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        drawPoints(g, translatedVertices);
        g.setColor(Color.GREEN);
        drawLines(g, translatedVertices);
    }

    public void drawPoints(Graphics g, int[][] vertices) {
        for (int[] vertex : vertices) {
            int pointSize = 4;
            int xCoordinate = vertex[0];
            int yCoordinate = vertex[1];
            g.fillRect(
                    xCoordinate - pointSize / 2,
                    yCoordinate - pointSize / 2,
                    pointSize,
                    pointSize);
        }
    }

    public void drawLines(Graphics g, int[][] vertices) {

        int[][] edges = {
                // lines connecting cube's front vertices
                // bottom line, right lines, top line, left line
                { 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 0 },
                // lines connecting cube's back vertices
                // bottom line, right line, top line, left line
                { 4, 5 }, { 5, 6 }, { 6, 7 }, { 7, 4 },
                // lines connecting cube's front vertices to back vertices
                // bottom left, bottom right, top right, top left
                { 0, 4 }, { 1, 5 }, { 2, 6 }, { 3, 7 }
        };
        for (int[] edge : edges) {
            g.drawLine(
                    // first point's x coordinate
                    vertices[edge[0]][0],
                    // first point's y coordinate
                    vertices[edge[0]][1],
                    // second point's x coordinate
                    vertices[edge[1]][0],
                    // second point's y coordinate
                    vertices[edge[1]][1]);

        }
    }

    public double[][] rotateY(double[][] vertices, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double[][] result = new double[vertices.length][3];

        for (int i = 0; i < vertices.length; i++) {
            double x = vertices[i][0];
            double y = vertices[i][1];
            double z = vertices[i][2];

            result[i][0] = x * cos + z * sin;
            result[i][1] = y;
            result[i][2] = -x * sin + z * cos;
        }
        return result;
    }

    public double[][] project(double[][] vertices) {
        double[][] projectedVertices = new double[vertices.length][2];
        for (int i = 0; i < vertices.length; i++) {
            // x' = x / (z + Z_NEAR)
            projectedVertices[i][0] = vertices[i][0] / (vertices[i][2] + Z_NEAR);
            // y' = y / (z + Z_NEAR)
            projectedVertices[i][1] = vertices[i][1] / (vertices[i][2] + Z_NEAR);
        }
        return projectedVertices;
    }

    public int[][] translate(double[][] coordinates) {
        // translates cartesian coordinates (-1,1) to graphics coordinates (0, width)
        // -1..1 => 0..2 => 0..2w => 0..w
        int[][] translatedVertices = new int[rotatedVertices.length][2];
        for (int i = 0; i < coordinates.length; i++) {
            translatedVertices[i][0] = (int) ((coordinates[i][0] + CUBE_SIDE_LENGTH) * screenWidth / 2);
            translatedVertices[i][1] = (int) ((-1 * coordinates[i][1] + CUBE_SIDE_LENGTH) * screenHeight / 2);
        }
        return translatedVertices;
    }

    public void printVertices() {
        System.out.println("VERTICES");
        print(rotatedVertices);
        projectedVertices = project(rotatedVertices);
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
