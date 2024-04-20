public class KahanSum {
    public static double sum(Double... fa) {
        Double sum = 0.0;
        Double compensation = 0.0;
        for (Double f : fa) {
            Double adj = f - compensation;
            Double temp = sum + adj;
            compensation = (temp - sum) - adj;
            sum = temp;
        }
        return sum;
    }
}