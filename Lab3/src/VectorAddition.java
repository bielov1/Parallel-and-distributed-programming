import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class VectorAddition implements Callable<Double[]>{
    private final Double[] _firstVec;
    private final Double[] _secondVec;

    private final Double[] result;

    private final ReentrantLock vecAddLock = new ReentrantLock();

    private final int _numThreads;
    private final String _log;

    public VectorAddition(Double[] firstVec, Double[] secondVec, String log, int numThreads) {
        this._firstVec = firstVec;
        this._secondVec = secondVec;
        this._numThreads = numThreads;
        this._log = log;
        this.result = new Double[firstVec.length];
    }

    private void compute(int index) {
        Double firstOperand = _firstVec[index];
        Double secondOperand = _secondVec[index];
        vecAddLock.lock();
        result[index] = KahanSum.sum(firstOperand, secondOperand);
        vecAddLock.unlock();
    }

    private void printResult() {
        ConsoleLogger.printVector(result, _log);
    }

    @Override
    public Double[] call() throws Exception {
        int n = _firstVec.length;
        ExecutorService exc = Executors.newFixedThreadPool(_numThreads);
        ArrayList<Callable<Object>> tasks = new ArrayList<>();

        for (int i = 0; i < _numThreads; i++) {
            final int start = i * (n / _numThreads);
            final int end = (i == _numThreads - 1)? n : start + (n / _numThreads);
            //Завдання, засноване на Anonymous Callable, яке обчислює суму векторів
            tasks.add(() -> {
                for (int j = start; j < end; j++) {
                    compute(j);
                }

                return null;
            });
        }

        List<Future<Object>> results = exc.invokeAll(tasks);

        for (Future<Object> res : results) {
            res.get();
        }

        exc.shutdown();

        printResult();

        return result;
    }
}
