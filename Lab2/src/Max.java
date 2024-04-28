import java.util.concurrent.Phaser;

public class Max {
    private final Double[] _vec;

    private final int _numThreads;
    private final String _log;
    private final Phaser _phaser;

    private final Double[] localMaxes;
    private Double globalMaximum = Double.MIN_VALUE;

    public Max(Double[] vec, String log, int numThreads) {
        this._vec = vec;
        this._log = log;
        this._numThreads = numThreads;
        this.localMaxes = new Double[numThreads];
        this._phaser = new Phaser(_numThreads);
    }

    public void parallelMaxFinder() throws InterruptedException {
        int n = _vec.length;

        for (int i = 0; i < _numThreads; i++) {
            final int start = i * (_vec.length / _numThreads);
            final int end = (i == _numThreads - 1) ? _vec.length : start + (_vec.length / _numThreads);
            final int threadIndex = i;

            Thread thread = new Thread(() -> {
                Double localMax = Double.MIN_VALUE;
                for (int j = start; j < end; j++) {
                    if (_vec[j] > localMax) {
                        localMax = _vec[j];
                    }
                }
                localMaxes[threadIndex] = localMax;
                _phaser.arrive();
            });

            thread.start();
        }

        _phaser.awaitAdvance(0);

        for (Double localMax : localMaxes) {
            if (localMax > globalMaximum) {
                globalMaximum = localMax;
            }
        }

        printResult();
    }

    public Double getResult() throws InterruptedException {
        return globalMaximum;
    }

    private void printResult() {
        ConsoleLogger.printScalar(globalMaximum, _log);
    }
}
