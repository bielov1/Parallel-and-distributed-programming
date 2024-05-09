import java.text.DecimalFormat;
import java.util.Random;

public class DataGenerator {
    public static final int MIN_PRECISION = 1;
    public static final int MAX_PRECISION = 17;
    public static final int MIN_VALUE = -100;
    public static final int MAX_VALUE = 100;

    public static Double[][] generateSquareMatrix(int size) {
        Double[][] matrix = new Double[size][size];
        Random generator = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = generateRandomDouble(generator);
            }
        }

        return matrix;
    }

    public static Double[] generateVector(int size) {
        Double[] vector = new Double[size];
        Random generator = new Random();

        for (int i = 0; i < size; i++) {
            vector[i] = generateRandomDouble(generator);
        }
        return vector;
    }

    public static Double generateRandomDouble(Random generator) {
        String format = "#." + "#".repeat(generator.nextInt(MIN_PRECISION, MAX_PRECISION + 1));
        DecimalFormat precisionFormat = new DecimalFormat(format);
        Double tempRandom = generator.nextDouble(MIN_VALUE, MAX_VALUE);

        return Double.parseDouble(precisionFormat.format(tempRandom).replace(",", "."));
    }
}
