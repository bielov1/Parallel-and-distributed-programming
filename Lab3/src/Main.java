import java.io.File;
import java.util.Random;
import java.util.concurrent.*;

/*
 * @author: Oleh Bielov, IO-11
 * @variant: 5 % 28 + 1 = 6
 */
public class Main {
    public static final String FILE_PREFIX = "./src/Data/";
    public static final String MDFile = FILE_PREFIX + "MD.json";
    public static final String MAFile = FILE_PREFIX + "MA.json";
    public static final String MTFile = FILE_PREFIX + "MT.json";
    public static final String MZFile = FILE_PREFIX + "MZ.json";
    public static final String MBFile = FILE_PREFIX + "MB.json";
    public static final String BFile = FILE_PREFIX + "B.json";
    public static final String EFile = FILE_PREFIX + "E.json";
    public static final String CFile = FILE_PREFIX + "C.json";
    public static final String aFile = FILE_PREFIX + "a.json";

    public static final int VECTOR_SIZE = 100;


    public static void main(String[] args) throws InterruptedException, ExecutionException {
        makeData();
        WorkingSet workingSet = new WorkingSet();
        setData(workingSet);
        long start = System.currentTimeMillis();
        compute(workingSet);
        long end = System.currentTimeMillis();
        serializeResults(workingSet);
        System.out.println("Time: " + (end - start) + " ms");
    }

    public static void compute(WorkingSet workingSet) throws InterruptedException, ExecutionException {
        //Виконувач
        ExecutorService exc = Executors.newFixedThreadPool(4);
        //Створення об'єктів в яких будуть зберігатися результати обчислення потоків
        Future<Double[]> bPlusC = exc.submit(
                new VectorAddition(workingSet.B, workingSet.C,"\nB+C=\n", 4));
        Future<Double[][]> mzMultMb = exc.submit(
                new MatrixMultiplication(workingSet.MZ, workingSet.MB, "\nMZ*MB=\n", 4));
        Future<Double[]> bMultMd = exc.submit(
                new MatrixVectorMultiplication(workingSet.MD, workingSet.B,"\nB*MD=\n",4));
        Future<Double[]> cMultMt = exc.submit(
                new MatrixVectorMultiplication(workingSet.MT, workingSet.C,"\nC*MT=\n", 4));

        Future<Double[][]> mdMultMt = exc.submit(
                new MatrixMultiplication(workingSet.MD, workingSet.MT,"\nMD*MT=\n",  4));
        Future<Double> bPlusCMax = exc.submit(
                new Max(bPlusC.get(), "\nmax(B+C)=\n", 4));
        Future<Double[]>cMultMtMultA = exc.submit(
                new VectorScalarMultiplication(cMultMt.get(), workingSet.a, "\nC*Mt*a=\n", 4));


        Future<Double[][]>MaxMultMdMultMt = exc.submit(
                new MatrixScalarMultiplication(mdMultMt.get(), bPlusCMax.get(),"\nmax(B+C)*MD*MT=\n",4));

        Future<Double[][]> MaxMultMdMultMtPlusMzMultMb = exc.submit(
                new MatrixAddition(MaxMultMdMultMt.get(), mzMultMb.get(),"\nMA: max(B+C)*MD*MT+MZ*MB=\n",4));
        Future<Double[]> bMultMdPlusCMultMtMultA = exc.submit(
                new VectorAddition(bMultMd.get(), cMultMtMultA.get(),"\nE: B*MD+C*MT*a=\n", 4));

        workingSet.MA = MaxMultMdMultMtPlusMzMultMb.get();
        workingSet.E = bMultMdPlusCMultMtMultA.get();

        exc.shutdown();
    }

    private static void makeData()  {
        boolean regenerateData = false;
        File[] files = {
                new File(MDFile), new File(MTFile), new File(MZFile), new File(MBFile),
                new File(BFile), new File(CFile), new File(aFile)
        };

        for (File f : files) {
            if (!f.exists()) {
                regenerateData = true;
                break;
            }
        }

        if (regenerateData) {
            Serializer.serializeMatrix(DataGenerator.generateSquareMatrix(VECTOR_SIZE), MDFile);
            Serializer.serializeMatrix(DataGenerator.generateSquareMatrix(VECTOR_SIZE), MTFile);
            Serializer.serializeMatrix(DataGenerator.generateSquareMatrix(VECTOR_SIZE), MZFile);
            Serializer.serializeMatrix(DataGenerator.generateSquareMatrix(VECTOR_SIZE), MBFile);
            Serializer.serializeVector(DataGenerator.generateVector(VECTOR_SIZE), BFile);
            Serializer.serializeVector(DataGenerator.generateVector(VECTOR_SIZE), CFile);
            Serializer.serializeScalar(DataGenerator.generateRandomDouble(new Random()), aFile);
        }
    }

    private static void setData(WorkingSet workingSet) {
        workingSet.MD = Serializer.deserializeMatrix(MDFile);
        workingSet.MT = Serializer.deserializeMatrix(MTFile);
        workingSet.MZ = Serializer.deserializeMatrix(MZFile);
        workingSet.MB = Serializer.deserializeMatrix(MBFile);
        workingSet.B = Serializer.deserializeVector(BFile);
        workingSet.C = Serializer.deserializeVector(CFile);
        workingSet.a = Serializer.deserializeScalar(aFile);
    }

    private static void serializeResults(WorkingSet workingSet) {
        Serializer.serializeMatrix(workingSet.MA, MAFile);
        Serializer.serializeVector(workingSet.E, EFile);
    }
}

