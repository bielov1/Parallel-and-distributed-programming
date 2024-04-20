public class ConsoleLogger {
    public synchronized static void printMatrix(int rowAmount, int colAmount, Double[][] matrix, String logMessage) {
        int[] maxLengths = new int[colAmount];

        for (int j = 0; j < colAmount; j++) {
            int maxLength = 0;
            for (int k = 0; k < rowAmount; k++) {
                int length = String.valueOf(matrix[k][j]).length();
                if (length > maxLength) {
                    maxLength = length;
                }
            }
            maxLengths[j] = maxLength;
        }

        System.out.println(logMessage);
        for (int i = 0; i < rowAmount; i++) {
            for (int j = 0; j < colAmount; j++) {
                String element = String.format("%" + maxLengths[j] + "s", matrix[i][j]);
                System.out.print(element + " ");
            }
            System.out.println();
        }
    }

    public synchronized static void printVector(Double[] vector, String logMessage) {
        int maxLength = 0;
        for (int i = 0; i < vector.length; i++) {
            int elementLength = String.valueOf(vector[i]).length();
            if (elementLength > maxLength) {
                maxLength = elementLength;
            }
        }

        System.out.println(logMessage);
        for (int i = 0; i < vector.length; i++) {
            String element = String.format("%" + maxLength + "s", vector[i]);
            System.out.print(element + " ");
        }
        System.out.println();
    }

    public synchronized static void printScalar(Double scalar, String logMessage) {
        System.out.println(logMessage);
        System.out.print(scalar);
    }
}
