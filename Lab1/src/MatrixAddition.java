import java.util.ArrayList;
import java.util.List;

public class MatrixAddition {

    private final Double[][] _firstMatrix;
    private final Double[][] _secondMatrix;

    private final Double[][] result;

    private final int _numThreads;
    private final String _log;
    public MatrixAddition(Double[][] fMat, Double[][] sMat, String log, int numThreads) {
        this._firstMatrix = fMat;
        this._secondMatrix = sMat;
        this._numThreads = numThreads;
        this._log = log;
        this.result = new Double[fMat.length][fMat[0].length];
    }

    public void parallelAddition() throws InterruptedException {
        int n = _firstMatrix.length;
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < _numThreads; i++) {
            final int startRow = i * (n / _numThreads);
            final int endRow = (i == _numThreads - 1)? n : startRow + (n / _numThreads);
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    for (int row = startRow; row < endRow; row++) {
                        for (int col = 0; col < _firstMatrix[row].length; col++) {
                            compute(row, col);
                        }
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        printResult();
    }

    public Double[][] getResult() {
        return result;
    }

    private void compute(int row, int column) {
        Double firstOperand = _firstMatrix[row][column];
        Double secondOperand = _secondMatrix[row][column];
        result[row][column] = KahanSum.sum(firstOperand, secondOperand);
    }

    private void printResult() {
        ConsoleLogger.printMatrix(result[0].length, result[0].length, result, _log);
    }
}
