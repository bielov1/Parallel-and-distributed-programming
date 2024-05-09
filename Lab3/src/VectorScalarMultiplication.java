import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class VectorScalarMultiplication implements Callable<Double[]>{
    private final Double[] _vector;
    private final Double _scalar;

    private final Double[] result;

    private final ReentrantLock vecScalMultLock = new ReentrantLock();

    private final int _numThreads;
    private final String _log;

    public VectorScalarMultiplication(Double[] vector, Double scalar, String log, int numThreads) {
        this._vector = vector;
        this._scalar = scalar;
        this._numThreads = numThreads;
        this._log = log;
        this.result = new Double[vector.length];
    }

    private void compute(int start, int end) {
        for (int i = start; i < end; i++) {
            vecScalMultLock.lock();
            result[i] = _vector[i] * _scalar;
            vecScalMultLock.unlock();
        }
    }

    private void printResult() {
        ConsoleLogger.printVector(result, _log);
    }

    @Override
    public Double[] call() throws InterruptedException, Exception {
        int n = _vector.length;
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
