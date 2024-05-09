import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

public class Max implements Callable<Double> {
    private final Double[] _vec;

    private final int _numThreads;
    private final String _log;

    private final Double[] localMaxes;
    private Double globalMaximum = Double.MIN_VALUE;

    public Max(Double[] vec, String log, int numThreads) {
        this._vec = vec;
        this._log = log;
        this._numThreads = numThreads;
        this.localMaxes = new Double[numThreads];
    }


    private void printResult() {
        ConsoleLogger.printScalar(globalMaximum, _log);
    }

    @Override
    public Double call() throws InterruptedException, Exception {
        int n = _vec.length;
        ExecutorService exc = Executors.newFixedThreadPool(_numThreads);
        ArrayList<Callable<Object>> tasks = new ArrayList<>();

        for (int i = 0; i < _numThreads; i++) {
            final int start = i * (_vec.length / _numThreads);
            final int end = (i == _numThreads - 1) ? _vec.length : start + (_vec.length / _numThreads);
            final int threadIndex = i;

            tasks.add(() -> {
                Double localMax = Double.MIN_VALUE;
                for (int j = start; j < end; j++) {
                    if (_vec[j] > localMax) {
                        localMax = _vec[j];
                    }
                }
                localMaxes[threadIndex] = localMax;

                return null;
            });
        }

        List<Future<Object>> results = exc.invokeAll(tasks);

        for (Future<Object> res : results) {
            res.get();
        }

        exc.shutdown();

        for (Double localMax : localMaxes) {
            if (localMax > globalMaximum) {
                globalMaximum = localMax;
            }
        }

        printResult();

        return globalMaximum;
    }
}
