import java.util.Date;

/**
 * IDATT2101 Algoritmer og datastrukturer
 * Oblig 1 - Rekursjon
 */
public class Main {
    public static void main(String[] args) {
        int runs;
        double x;
        int n;

        // Testing for correct values
        System.out.println("Testing for correct result:");
        System.out.printf("Algorithm 1: (2^10) value: %.4f%n", recursiveTest1(2, 10));
        System.out.printf("Algorithm 2: (3^14) value: %.4f%n", recursiveTest2(3, 14));
        System.out.printf("Math.pow: (2^10) & (3^14) values: %.4f, %.4f%n\n", Math.pow(2, 10), Math.pow(3, 14));

        runs = (int) 1e7;

        // Timed tests 1
        x = 10000;
        n = 10;

        System.out.printf("\nTimed Test 1 where x=%.2f & n=%d%n", x, n);
        benchmark(runs, x, n);


        // Timed tests 2
        x = 10000;
        n = 100;

        System.out.printf("\nTimed Test 2 where x=%.2f & n=%d%n", x, n);
        benchmark(runs, x, n);


        // Timed tests 3
        x = 10000;
        n = 1000;

        System.out.printf("\nTimed Test 3 where x=%.2f & n=%d%n", x, n);
        benchmark(runs, x, n);


        // Timed tests 4
        x = 10000;
        n = 10000;

        System.out.printf("\nTimed Test 4 where x=%.2f & n=%d%n", x, n);
        benchmark(runs, x, n);
    }

    /**
     * Runs a benchmark test between the two recursive tests and Math.pow, and outputs execution time.
     *
     * @param runs Number of runs
     * @param x Value x (double)
     * @param n Exponent (integer)
     */
    public static void benchmark(int runs, double x, int n) {
        Date startTime;
        Date endTime;

        // Test 1
        startTime = new Date();
        for (int i = 0; i < runs; i++) {
            recursiveTest1(x, n);
        }
        endTime = new Date();
        System.out.printf("Algorithm 1 (running %d times): %d ms%n", runs, (endTime.getTime() - startTime.getTime()));

        // Test 2
        startTime = new Date();
        for (int i = 0; i < runs; i++) {
            recursiveTest2(x, n);
        }
        endTime = new Date();
        System.out.printf("Algorithm 2 (running %d times): %d ms%n", runs, (endTime.getTime() - startTime.getTime()));

        // Test 3 with Math.pow
        startTime = new Date();
        for (int i = 0; i < runs; i++) {
            Math.pow(x, n);
        }
        endTime = new Date();
        System.out.printf("Math.pow (running %d times): %d ms%n", runs, (endTime.getTime() - startTime.getTime()));
    }

    /**
     * Task 2.1-1 page 28.
     * x^n = { 1 when n=0, x*x^n-1 when n > 0 }
     *
     * @param x Value x (double)
     * @param n Exponent (integer)
     * @return Calculated value
     */
    public static double recursiveTest1(double x, int n) {
        // x*x^n-1 when n > 0
        if (n > 0) {
            return x * recursiveTest1(x, n - 1);
        }

        // 1 when n = 0
        return 1;
    }

    /**
     * Task 2.2-3 page 38.
     *
     * @param x Value x (double)
     * @param n Exponent (integer)
     * @return Calculated value
     */
    public static double recursiveTest2(double x, int n) {
        if (n > 0) {
            // Even: (x^2)^(n/2)
            if (n % 2 == 0) {
                return recursiveTest2(x*x, n/2);
            }

            // Odd: x * (x^2)^((n-1)/2)
            else {
                return x * recursiveTest2(x*x, (n - 1)/2);
            }
        }

        // 1 when n = 0
        return 1;
    }
}
