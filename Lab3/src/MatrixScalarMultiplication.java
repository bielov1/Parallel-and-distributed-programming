import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class MatrixScalarMultiplication implements Callable<Double[][]>{
    private final Double _scalar;
    private final Double[][] _matrix;

    private final Double[][] result;

    private final ReentrantLock matScalMultLock = new ReentrantLock();

    private final int _numThreads;
    private final String _log;

    public MatrixScalarMultiplication(Double[][] matrix, Double scalar, String log, int numThreads) {
        this._matrix = matrix;
        this._scalar = scalar;
        this._numThreads = numThreads;
        this._log = log;
        this.result = new Double[matrix.length][matrix[0].length];
    }

    private void compute(int start, int end) {
        for (int row = start; row < end; row++) {
            for (int col = 0; col < _matrix[row].length; col++) {
                matScalMultLock.lock();
                result[row][col] = _matrix[row][col] * _scalar;
                matScalMultLock.unlock();
            }
        }
    }

    private void printResult() {
        ConsoleLogger.printMatrix(result[0].length, result[0].length, result, _log);
    }

    @Override
    public Double[][] call() throws InterruptedException, Exception {
        int n = _matrix.length;
        ExecutorService exc = Executors.newFixedThreadPool(_numThreads);
        ArrayList<Callable<Object>> tasks = new ArrayList<>();

        for (int i = 0; i < _numThreads; i++) {
            final int start = i * (n / _numThreads);
            final int end = (i == _numThreads - 1)? n : start + (n / _numThreads);
            tasks.add(() -> {
                compute(start, end);
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
