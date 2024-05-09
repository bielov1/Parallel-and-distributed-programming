import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class MatrixAddition implements Callable<Double[][]> {

    private final Double[][] _firstMatrix;
    private final Double[][] _secondMatrix;

    private final Double[][] result;

    private final ReentrantLock matAddLock = new ReentrantLock();

    private final int _numThreads;
    private final String _log;
    public MatrixAddition(Double[][] fMat, Double[][] sMat, String log, int numThreads) {
        this._firstMatrix = fMat;
        this._secondMatrix = sMat;
        this._numThreads = numThreads;
        this._log = log;
        this.result = new Double[fMat.length][fMat[0].length];

    }

    private void compute(int row, int column) {
        Double firstOperand = _firstMatrix[row][column];
        Double secondOperand = _secondMatrix[row][column];
        matAddLock.lock();
        result[row][column] = KahanSum.sum(firstOperand, secondOperand);
        matAddLock.unlock();
    }

    private void printResult() {
        ConsoleLogger.printMatrix(result[0].length, result[0].length, result, _log);
    }

    @Override
    public Double[][] call() throws InterruptedException, Exception {
        int n = _firstMatrix.length;
        ExecutorService exc = Executors.newFixedThreadPool(_numThreads);
        ArrayList<Callable<Object>> tasks = new ArrayList<>();

        for (int i = 0; i < _numThreads; i++) {
            final int startRow = i * (n / _numThreads);
            final int endRow = (i == _numThreads - 1)? n : startRow + (n / _numThreads);
            tasks.add(() -> {
                for (int row = startRow; row < endRow; row++) {
                    for (int col = 0; col < _firstMatrix[row].length; col++) {
                        compute(row, col);
                    }
                }

                return null;
            });
        }

        List<Future<Object>> results = exc.invokeAll(tasks);

        for (Future<Object> res : results) {
            res.get();
        }

        exc.shutdown();

        printResult();

        return result;
    }
}
