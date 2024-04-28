import java.util.concurrent.Phaser;

public class MatrixScalarMultiplication {
    private final Double _scalar;
    private final Double[][] _matrix;
    private final Double[][] result;

    private final int _numThreads;
    private final String _log;
    private final Phaser _phaser;

    public MatrixScalarMultiplication(Double[][] matrix, Double scalar, String log, int numThreads) {
        this._matrix = matrix;
        this._scalar = scalar;
        this._numThreads = numThreads;
        this._log = log;
        this.result = new Double[matrix.length][matrix[0].length];
        this._phaser = new Phaser(_numThreads);
    }

    public void parallelMultiplication() throws InterruptedException {
        int n = _matrix.length;

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
            for (int col = 0; col < _matrix[row].length; col++) {
                result[row][col] = _matrix[row][col] * _scalar;

            }
        }
    }

    private void printResult() {
        ConsoleLogger.printMatrix(result[0].length, result[0].length, result, _log);
    }
}
