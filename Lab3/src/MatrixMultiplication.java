import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class MatrixMultiplication implements Callable<Double[][]> {

    private final Double[][] _firstMatrix;
    private final Double[][] _secondMatrix;

    private final Double[][] result;

    public final ReentrantLock matMultLock = new ReentrantLock();

    private final int _numThreads;
    private final String _log;

    public MatrixMultiplication(Double[][] fMat, Double[][] sMat, String log, int numThreads) {
        this._firstMatrix = fMat;
        this._secondMatrix = sMat;
        this._numThreads = numThreads;
        this._log = log;
        this.result = new Double[fMat[0].length][sMat.length];
    }

    private void compute(int start, int end) {
        for (int row = start; row < end; row++) {
            for (int col = 0; col < _secondMatrix[0].length; col++) {
                Double[] sumParts = new Double[_firstMatrix[0].length];
                for (int k = 0; k < _firstMatrix[row].length; k++) {
                    sumParts[k] = _firstMatrix[row][k] * _secondMatrix[k][col];
                }
                matMultLock.lock();
                result[row][col] = KahanSum.sum(sumParts);
                matMultLock.unlock();
            }
        }
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
            final int start = i * (n / _numThreads);
            final int end = (i == _numThreads - 1)? n : start + (n / _numThreads);
            //Завдання, засноване на Anonymous Callable, яке обчислює множення матриць
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
