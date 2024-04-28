import java.util.concurrent.Phaser;

public class MatrixMultiplication {

    private final Double[][] _firstMatrix;
    private final Double[][] _secondMatrix;

    private final Double[][] result;

    private final int _numThreads;
    private final String _log;
    private final Phaser _phaser;
    public MatrixMultiplication(Double[][] fMat, Double[][] sMat, String log, int numThreads) {
        this._firstMatrix = fMat;
        this._secondMatrix = sMat;
        this._numThreads = numThreads;
        this._log = log;
        this.result = new Double[fMat[0].length][sMat.length];
        this._phaser = new Phaser(_numThreads);
    }

    public void parallelMultiplication() throws InterruptedException {
        int n = _firstMatrix.length;
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

    public Double[][] getResult() throws InterruptedException {
        return result;
    }

    private void compute(int start, int end) {
        for (int row = start; row < end; row++) {
            for (int col = 0; col < _secondMatrix[0].length; col++) {
                Double[] sumParts = new Double[_firstMatrix[0].length];
                for (int k = 0; k < _firstMatrix[row].length; k++) {
                    sumParts[k] = _firstMatrix[row][k] * _secondMatrix[k][col];
                }
                result[row][col] = KahanSum.sum(sumParts);
            }
        }
    }

    private void printResult() {
        ConsoleLogger.printMatrix(result[0].length, result[0].length, result, _log);
    }
}
