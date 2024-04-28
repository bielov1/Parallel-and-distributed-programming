import java.io.File;
import java.util.Random;
import java.util.concurrent.Phaser;

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


    public static void main(String[] args) throws InterruptedException {
        makeData();
        WorkingSet workingSet = new WorkingSet();
        setData(workingSet);
        long start = System.currentTimeMillis();
        compute(workingSet);
        long end = System.currentTimeMillis();
        serializeResults(workingSet);
        System.out.println("Time: " + (end - start) + " ms");
    }

    public static void compute(WorkingSet workingSet) throws InterruptedException {
        //Реєструємо 4 фаз
        Phaser firstPhaser = new Phaser(4);
        //Створення об'єктів в яких будуть зберігатися результати обчислення потоків
        VectorAddition bPlusC = new VectorAddition(workingSet.B, workingSet.C,"\nB+C=\n", 4);
        MatrixMultiplication mzMultMb = new MatrixMultiplication(workingSet.MZ, workingSet.MB, "\nMZ*MB=\n", 4);
        MatrixVectorMultiplication bMultMd = new MatrixVectorMultiplication(workingSet.MD, workingSet.B,"\nB*MD=\n",4);
        MatrixVectorMultiplication cMultMt = new MatrixVectorMultiplication(workingSet.MT, workingSet.C,"\nC*MT=\n", 4);

        //Створення потоків на основі anonymous Runnable
        Thread bPlusCThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bPlusC.parallelAddition();
                    firstPhaser.arriveAndDeregister();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread mzMultMbThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mzMultMb.parallelMultiplication();
                    firstPhaser.arriveAndDeregister();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread bMultMdThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bMultMd.parallelMultiplication();
                    firstPhaser.arriveAndDeregister();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread cMultMtThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cMultMt.parallelMultiplication();
                    firstPhaser.arriveAndDeregister();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        bPlusCThread.start();
        mzMultMbThread.start();
        bMultMdThread.start();
        cMultMtThread.start();

        firstPhaser.awaitAdvance(0);
        // Реєструємо 3 фази
        Phaser secondPhaser = new Phaser(3);

        MatrixMultiplication mdMultMt = new MatrixMultiplication(workingSet.MD, workingSet.MT,"\nMD*MT=\n",  4);
        Max bPlusCMax = new Max(bPlusC.getResult(), "\nmax(B+C)=\n", 4);
        VectorScalarMultiplication cMultMtMultA = new VectorScalarMultiplication(cMultMt.getResult(), workingSet.a, "\nC*Mt*a=\n", 4);

        Thread mdMultMtThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mdMultMt.parallelMultiplication();
                    secondPhaser.arriveAndDeregister();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread bPlusCMaxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bPlusCMax.parallelMaxFinder();
                    secondPhaser.arriveAndDeregister();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread cMultMtMultAThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cMultMtMultA.parallelMultiplication();
                    secondPhaser.arriveAndDeregister();
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        mdMultMtThread.start();
        bPlusCMaxThread.start();
        cMultMtMultAThread.start();

        secondPhaser.awaitAdvance(0);

        // Реєструємо 1 фазу
        Phaser thirdPhaser = new Phaser(1);

        MatrixScalarMultiplication MaxMultMdMultMt = new MatrixScalarMultiplication(mdMultMt.getResult(), bPlusCMax.getResult(),"\nmax(B+C)*MD*MT=\n",4);


        Thread MaxMultMdMultMtThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MaxMultMdMultMt.parallelMultiplication();
                    thirdPhaser.arriveAndDeregister();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        MaxMultMdMultMtThread.start();

        thirdPhaser.awaitAdvance(0);

        // Реєструємо 2 фази
        Phaser finalPhaser = new Phaser(2);
        MatrixAddition MaxMultMdMultMtPlusMzMultMb = new MatrixAddition(MaxMultMdMultMt.getResult(), mzMultMb.getResult(),"\nMA: max(B+C)*MD*MT+MZ*MB=\n",4);
        VectorAddition bMultMdPlusCMultMtMultA = new VectorAddition(bMultMd.getResult(), cMultMtMultA.getResult(),"\nE: B*MD+C*MT*a=\n", 4);

        Thread MaxMultMdMultMtPlusMzMultMbThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MaxMultMdMultMtPlusMzMultMb.parallelAddition();
                    finalPhaser.arriveAndDeregister();
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread bMultMdPlusCMultMtMultAThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bMultMdPlusCMultMtMultA.parallelAddition();
                    finalPhaser.arriveAndDeregister();
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        MaxMultMdMultMtPlusMzMultMbThread.start();
        bMultMdPlusCMultMtMultAThread.start();

        finalPhaser.awaitAdvance(0);

        workingSet.MA = MaxMultMdMultMtPlusMzMultMb.getResult();
        workingSet.E = bMultMdPlusCMultMtMultA.getResult();
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

