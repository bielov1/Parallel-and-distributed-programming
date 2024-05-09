import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class MatrixVectorMultiplication implements Callable<Double[]> {
    private final Double[][] _matrix;
    private final Double[] _vector;

    private final Double[] result;

    private final ReentrantLock matVecMultLock = new ReentrantLock();

    private final int _numThreads;
    private final String _log;
    public MatrixVectorMultiplication(Double[][] matrix, Double[] vector, String log, int numThreads) {
        this._matrix = matrix;
        this._vector = vector;
        this._numThreads = numThreads;
        this._log = log;
        this.result = new Double[matrix.length];
    }

    private void printResult() {
        ConsoleLogger.printVector(result, _log);
    }

    @Override
    public Double[] call() throws InterruptedException, Exception {
        int n = _matrix.length;
        ExecutorService exc = Executors.newFixedThreadPool(_numThreads);
        ArrayList<Callable<Object>> tasks = new ArrayList<>();

        for (int i = 0; i < _numThreads; i++) {
            final int start = i * (n / _numThreads);
            final int end = (i == _numThreads - 1)? n : start + (n / _numThreads);
            tasks.add(() -> {
                for (int col = start; col < end; col++) {
                    Double[] sumParts = new Double[_vector.length];
                    for (int ele = 0; ele < _vector.length; ele++) {
                        sumParts[ele] = _vector[ele] * _matrix[ele][col];
                    }
                    matVecMultLock.lock();
                    result[col] = KahanSum.sum(sumParts);
                    matVecMultLock.unlock();
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
