import java.util.ArrayList;
import java.util.List;

public class VectorAddition {
    private final Double[] _firstVec;
    private final Double[] _secondVec;
    private final Double[] result;

    private final int _numThreads;
    private final String _log;

    public VectorAddition(Double[] firstVec, Double[] secondVec, String log, int numThreads) {
        this._firstVec = firstVec;
        this._secondVec = secondVec;
        this._numThreads = numThreads;
        this._log = log;
        this.result = new Double[firstVec.length];
    }

    public void parallelAddition() throws InterruptedException {
        int n = _firstVec.length;
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < _numThreads; i++) {
            final int start = i * (n / _numThreads);
            final int end = (i == _numThreads - 1)? n : start + (n / _numThreads);
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    for (int i = start; i < end; i++) {
                        compute(i);
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


    private void compute(int index) {
        Double firstOperand = _firstVec[index];
        Double secondOperand = _secondVec[index];
        result[index] = KahanSum.sum(firstOperand, secondOperand);
    }

    private void printResult() {
        ConsoleLogger.printVector(result, _log);
    }
}
