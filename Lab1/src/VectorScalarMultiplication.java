import java.util.ArrayList;
import java.util.List;

public class VectorScalarMultiplication {
    private final Double[] _vector;
    private final Double _scalar;
    private final Double[] result;
    private final int _numThreads;
    private final String _log;

    public VectorScalarMultiplication(Double[] vector, Double scalar, String log,  int numThreads) {
        this._vector = vector;
        this._scalar = scalar;
        this._numThreads = numThreads;
        this._log = log;
        this.result = new Double[vector.length];
    }

    public void parallelMultiplication() throws InterruptedException {
        int n = _vector.length;
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < _numThreads; i++) {
            final int start = i * (n / _numThreads);
            final int end = (i == _numThreads - 1)? n : start + (n / _numThreads);
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    compute(start, end);
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

    public Double[] getResult() {
        return result;
    }

    private void compute(int start, int end) {
        for (int i = start; i < end; i++) {
            result[i] = KahanSum.sum(_scalar, _vector[i]);
        }
    }

    private void printResult() {
        ConsoleLogger.printVector(result, _log);
    }
}
