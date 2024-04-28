import java.util.concurrent.Phaser;

public class VectorScalarMultiplication {
    private final Double[] _vector;
    private final Double _scalar;
    private final Double[] result;
    private final int _numThreads;
    private final String _log;
    private final Phaser _phaser;

    public VectorScalarMultiplication(Double[] vector, Double scalar, String log, int numThreads) {
        this._vector = vector;
        this._scalar = scalar;
        this._numThreads = numThreads;
        this._log = log;
        this.result = new Double[vector.length];
        this._phaser = new Phaser(_numThreads);
    }

    public void parallelMultiplication() throws InterruptedException {
        int n = _vector.length;
        for (int i = 0; i < _numThreads; i++) {
            final int start = i * (n / _numThreads);
            final int end = (i == _numThreads - 1)? n : start + (n / _numThreads);
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    compute(start, end);
                    _phaser.arrive();
                }
            });
            thread.start();
        }

        _phaser.awaitAdvance(0);

        printResult();
    }

    public Double[] getResult() {
        return result;
    }

    //виправив логіку коду
    private void compute(int start, int end) {
        for (int i = start; i < end; i++) {
            result[i] = _vector[i] * _scalar;
        }
    }

    private void printResult() {
        ConsoleLogger.printVector(result, _log);
    }
}
