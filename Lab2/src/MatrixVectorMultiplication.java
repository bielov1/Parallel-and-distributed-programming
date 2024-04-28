import java.util.concurrent.Phaser;

public class MatrixVectorMultiplication {
    private final Double[][] _matrix;
    private final Double[] _vector;
    private final Double[] result;

    private final int _numThreads;
    private final String _log;
    private final Phaser _phaser;
    public MatrixVectorMultiplication(Double[][] matrix, Double[] vector, String log, int numThreads) {
        this._matrix = matrix;
        this._vector = vector;
        this._numThreads = numThreads;
        this._log = log;
        this.result = new Double[matrix.length];
        this._phaser = new Phaser(_numThreads);
    }

    public void parallelMultiplication() throws InterruptedException {
        int n = _matrix.length;
        for (int i = 0; i < _numThreads; i++) {
            final int start = i * (n / _numThreads);
            final int end = (i == _numThreads - 1)? n : start + (n / _numThreads);
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    //виправив логіку коду
                    for (int col = start; col < end; col++) {
                        Double[] sumParts = new Double[_vector.length];
                        for (int ele = 0; ele < _vector.length; ele++) {
                            sumParts[ele] = _vector[ele] * _matrix[ele][col];
                        }
                        result[col] = KahanSum.sum(sumParts);
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

    private void printResult() {
        ConsoleLogger.printVector(result, _log);
    }
}
