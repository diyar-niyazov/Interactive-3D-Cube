import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {
    private String filePath;
    private double[][] vertices;

    public double[][] parseFile(String filePath) {
        ArrayList<double[]> parsedVertices = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("v ")) {
                    String[] tokens = line.split("\\s+");
                    parsedVertices.get(i)[0] = Double.valueOf(tokens[1]);
                    parsedVertices.get(i)[1] = Double.valueOf(tokens[2]);
                    parsedVertices.get(i)[2] = Double.valueOf(tokens[3]);
                    i++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vertices.toArray(new double[parsedVertices.size][3]);
    }
}