import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Panel extends JPanel implements KeyListener {
    private static final double CUBE_SIDE_LENGTH = 1;
    // distance from the camera to the projection plane
    private static final int FPS = 60;

    private final int screenWidth, screenHeight;
    private double Z_NEAR = 3;
    private boolean showPoints = false;
    private Color lineColor = Color.GREEN;

    private boolean initialRotatingX = false;
    private boolean rotatingX = false;
    private double xAngle = 0;
    private double xAngleIncrement = Math.PI / FPS / 2;

    private boolean initialRotatingY = true;
    private boolean rotatingY = true;
    private double yAngle = 0;
    private double yAngleIncrement = -Math.PI / FPS / 2;

    private double[][] initialVertices;
    private double[][] rotatedVertices;
    private double[][] projectedVertices;
    private int[][] translatedVertices;

    public Panel(int frameWidth, int frameHeight) {
        setFocusable(true);
        addKeyListener(this);
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
            rotatedVertices = initialVertices;
            if (rotatingX) {
                xAngle += xAngleIncrement;
            }
            if (rotatingY) {
                yAngle += yAngleIncrement;
            }
            rotatedVertices = rotateY(rotateX(initialVertices, xAngle), yAngle);

            projectedVertices = project(rotatedVertices);
            translatedVertices = translate(projectedVertices);
            repaint();
        });
        timer.start();

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (showPoints) {
            g.setColor(Color.WHITE);
            drawPoints(g, translatedVertices);
        }
        g.setColor(lineColor);
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

    public double[][] rotateY(double[][] vertices, double yAngle) {
        double cos = Math.cos(yAngle);
        double sin = Math.sin(yAngle);

        double[][] rotatedVertices = new double[vertices.length][3];

        for (int i = 0; i < vertices.length; i++) {
            double x = vertices[i][0];
            double y = vertices[i][1];
            double z = vertices[i][2];

            rotatedVertices[i][0] = x * cos + z * sin;
            rotatedVertices[i][1] = y;
            rotatedVertices[i][2] = -x * sin + z * cos;
        }
        return rotatedVertices;
    }

    public double[][] rotateX(double[][] vertices, double xAngle) {
        double cos = Math.cos(xAngle);
        double sin = Math.sin(xAngle);

        double[][] rotatedVertices = new double[vertices.length][3];

        for (int i = 0; i < vertices.length; i++) {
            double x = vertices[i][0];
            double y = vertices[i][1];
            double z = vertices[i][2];

            rotatedVertices[i][0] = x;
            rotatedVertices[i][1] = y * cos - z * sin;
            rotatedVertices[i][2] = y * sin + z * cos;
        }
        return rotatedVertices;
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

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        int keyCode = ke.getKeyCode();

        if (keyCode == KeyEvent.VK_SPACE) {
            if (rotatingX || rotatingY) {
                initialRotatingX = rotatingX;
                initialRotatingY = rotatingY;
                rotatingX = false;
                rotatingY = false;
            } else {
                rotatingX = initialRotatingX;
                rotatingY = initialRotatingY;
            }
        }

        boolean rotatingLeft = yAngleIncrement > 0;
        if (keyCode == KeyEvent.VK_RIGHT) {
            if (!rotatingY) {
                yAngleIncrement = -1 * Math.abs(yAngleIncrement);
                rotatingY = true;
            } else if (rotatingY && rotatingLeft) {
                rotatingY = false;
            }
        } else if (keyCode == KeyEvent.VK_LEFT) {
            if (!rotatingY) {
                yAngleIncrement = Math.abs(yAngleIncrement);
                rotatingY = true;
            } else if (rotatingY && !rotatingLeft) {
                rotatingY = false;
            }
        }

        boolean rotatingUp = xAngleIncrement > 0;
        if (keyCode == KeyEvent.VK_UP) {
            if (!rotatingX) {
                xAngleIncrement = Math.abs(xAngleIncrement);
                rotatingX = true;
            } else if (rotatingX && !rotatingUp) {
                rotatingX = false;
            }
        } else if (keyCode == KeyEvent.VK_DOWN) {
            if (!rotatingX) {
                xAngleIncrement = -1 * Math.abs(xAngleIncrement);
                rotatingX = true;
            } else if (rotatingX && rotatingUp) {
                rotatingX = false;
            }
        }

        double MIN_Z_NEAR = 2.5;
        double MAX_Z_NEAR = 50;
        double Z_NEAR_INCREMENT = 0.1;
        if (keyCode == KeyEvent.VK_W) {
            if (Z_NEAR > MIN_Z_NEAR)
                Z_NEAR -= Z_NEAR_INCREMENT;
        } else if (keyCode == KeyEvent.VK_S) {
            if (Z_NEAR < MAX_Z_NEAR)
                Z_NEAR += Z_NEAR_INCREMENT;
        }

	if (keyCode == KeyEvent.VK_PERIOD) {
		xAngleIncrement *= 1.25;
		yAngleIncrement *= 1.25;
	} else if (keyCode == KeyEvent.VK_COMMA) {
		xAngleIncrement *= 0.75;
		yAngleIncrement *= 0.75;
	}

        lineColor = switch (keyCode) {
            case KeyEvent.VK_1 -> Color.RED;
            case KeyEvent.VK_2 -> Color.ORANGE;
            case KeyEvent.VK_3 -> Color.YELLOW;
            case KeyEvent.VK_4 -> Color.GREEN;
            case KeyEvent.VK_5 -> Color.BLUE;
            case KeyEvent.VK_6 -> Color.MAGENTA;
            case KeyEvent.VK_7 -> Color.PINK;
            case KeyEvent.VK_8 -> Color.DARK_GRAY;
            case KeyEvent.VK_9 -> Color.LIGHT_GRAY;
            case KeyEvent.VK_0 -> Color.WHITE;
            default -> lineColor;
        };

        if (keyCode == KeyEvent.VK_EQUALS) {
            showPoints = true;
        } else if (keyCode == KeyEvent.VK_MINUS) {
            showPoints = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
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
