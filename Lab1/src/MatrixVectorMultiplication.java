import java.util.ArrayList;
import java.util.List;

public class MatrixVectorMultiplication {
    private final Double[][] _matrix;
    private final Double[] _vector;
    private final Double[] result;

    private final int _numThreads;
    private final String _log;
    public MatrixVectorMultiplication(Double[][] matrix, Double[] vector, String log, int numThreads) {
        this._matrix = matrix;
        this._vector = vector;
        this._numThreads = numThreads;
        this._log = log;
        this.result = new Double[matrix.length];
    }

    public void parallelMultiplication() throws InterruptedException {
        int n = _matrix.length;
        List<Thread> threads = new ArrayList<>();
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

    public Double[] getResult() throws InterruptedException {
        return result;
    }

    private void printResult() {
        ConsoleLogger.printVector(result, _log);
    }
}
