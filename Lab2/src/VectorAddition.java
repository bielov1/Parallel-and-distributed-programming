import java.util.concurrent.Phaser;


public class VectorAddition {
    private final Double[] _firstVec;
    private final Double[] _secondVec;
    private final Double[] result;

    private final int _numThreads;
    private final String _log;
    private final Phaser _phaser;

    public VectorAddition(Double[] firstVec, Double[] secondVec, String log, int numThreads) {
        this._firstVec = firstVec;
        this._secondVec = secondVec;
        this._numThreads = numThreads;
        this._log = log;
        this.result = new Double[firstVec.length];
        this._phaser = new Phaser(_numThreads);
    }

    public void parallelAddition() throws InterruptedException {
        int n = _firstVec.length;
        for (int i = 0; i < _numThreads; i++) {
            final int start = i * (n / _numThreads);
            final int end = (i == _numThreads - 1)? n : start + (n / _numThreads);
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    for (int i = start; i < end; i++) {
                        compute(i);
                    }
                    _phaser.arrive();
                }
            });
            thread.start();
        }

        _phaser.awaitAdvance(0);

        printResult();
    }

    public Double[] getResult() throws InterruptedException {
        return result;
    }


    private void compute(int index) {
        Double firstOperand = _firstVec[index];
        Double secondOperand = _secondVec[index];
        result[index] = KahanSum.sum(firstOperand, secondOperand);
    }

    private void printResult() {
        ConsoleLogger.printVector(result, _log);
    }
}
