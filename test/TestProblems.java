import java.util.*;
import uq.comp3506.a1.Problems;

/**
 * Run with:  java -ea TestProblems
 */
public class TestProblems {

    /* ==== tiny assert helper ==== */
    private static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    /* =========================================================
     * Short Runs (RLE)
     * ========================================================= */

    private static String rleOracle(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        int i = 0, n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            int j = i + 1;
            while (j < n && s.charAt(j) == c) j++;
            int run = j - i;
            // chunk into 1..9
            while (run > 9) { out.append(c).append('9'); run -= 9; }
            out.append(c).append((char) ('0' + run));
            i = j;
        }
        return out.toString();
    }

    private static void testShortRunsUnit() {
        check(Problems.shortRuns("HELLOOOO").equals("H1E1L2O4"), "RLE: HELLOOOO");
        check(Problems.shortRuns("AAAAAAAA").equals("A8"), "RLE: 8 A");
        check(Problems.shortRuns("VERYSAD").equals("V1E1R1Y1S1A1D1"), "RLE: singles");
        check(Problems.shortRuns("AAAAAAAAAAAA").equals("A9A3"), "RLE: 12 A");
        check(Problems.shortRuns("").equals(""), "RLE: empty");
        check(Problems.shortRuns("A").equals("A1"), "RLE: single");
    }

    private static void testShortRunsFuzz() {
        Random r = new Random(123);
        for (int rep = 0; rep < 2000; rep++) {
            int n = r.nextInt(0, 200);
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < n; i++) s.append((char) ('A' + r.nextInt(4)));
            String exp = rleOracle(s.toString());
            String got = Problems.shortRuns(s.toString());
            if (!Objects.equals(exp, got)) {
                throw new AssertionError("RLE mismatch\ns=" + s + "\nexp=" + exp + "\ngot=" + got);
            }
        }
    }

    /* =========================================================
     * Arithmetic Rules
     * ========================================================= */

    private static long arithmeticOracle(Long[] A, long t) {
        PriorityQueue<Long> pq = new PriorityQueue<>(Comparator.reverseOrder());
        Collections.addAll(pq, A);
        long score = 0L;
        for (long i = 0; i < t; i++) {
            long best = pq.remove();
            score += best;
            pq.add(best + 1);
        }
        return score;
    }

    private static void testArithmeticRulesUnit() {
        check(arithmeticOracle(new Long[]{1L, 2L, 3L}, 3)
                        == Problems.arithmeticRules(new Long[]{1L, 2L, 3L}, 3),
                "AR small");
        check(arithmeticOracle(new Long[]{5L}, 4)
                        == Problems.arithmeticRules(new Long[]{5L}, 4),
                "AR single");
        check(arithmeticOracle(new Long[]{0L, 0L, 0L}, 5)
                        == Problems.arithmeticRules(new Long[]{0L, 0L, 0L}, 5),
                "AR zeros");
        check(arithmeticOracle(new Long[]{10L, 9L, 8L}, 1_0)
                        == Problems.arithmeticRules(new Long[]{10L, 9L, 8L}, 10),
                "AR mid");
    }

    private static void testArithmeticRulesFuzz() {
        Random r = new Random(42);
        for (int rep = 0; rep < 400; rep++) {
            int n = r.nextInt(1, 60);
            long t = r.nextInt(0, 400);
            Long[] A = new Long[n];
            for (int i = 0; i < n; i++) A[i] = (long) r.nextInt(0, 200); // non-negative per spec
            long exp = arithmeticOracle(A.clone(), t);
            long got = Problems.arithmeticRules(A.clone(), t);
            if (exp != got) {
                throw new AssertionError("ArithmeticRules mismatch exp=" + exp + " got=" + got);
            }
        }
    }

    /* =========================================================
     * SQRT Happens
     * ========================================================= */

    private static void testSqrtUnit() {
        double y = Problems.sqrtHappens(10, 0.1);
        check(Math.abs(10 - y * y) <= 0.1 + 1e-12, "sqrt 10 eps=0.1");
        y = Problems.sqrtHappens(17, 0.001);
        check(Math.abs(17 - y * y) <= 0.001 + 1e-12, "sqrt 17 eps=0.001");
        check(Problems.sqrtHappens(0, 1e-6) == 0.0, "sqrt 0");
    }

    private static void testSqrtFuzz() {
        Random r = new Random(7);
        for (int i = 0; i < 1000; i++) {
            long n = r.nextInt(0, 1_000_000);
            double eps = Math.pow(10, -r.nextInt(1, 6)); // 1e-1..1e-5
            double y = Problems.sqrtHappens(n, eps);
            if (!(Math.abs(n - y * y) <= eps + 1e-9)) {
                throw new AssertionError("sqrt residual too large: n=" + n + " eps=" + eps + " y=" + y);
            }
        }
    }

    /* =========================================================
     * Space Oddity
     * ========================================================= */

    private static long spaceOddityOracle(Long[] A) {
        Map<Long, Integer> parity = new HashMap<>();
        for (long v : A) parity.put(v, (parity.getOrDefault(v, 0) ^ 1));
        long best = -1;
        for (Map.Entry<Long, Integer> e : parity.entrySet()) {
            if (e.getValue() == 1) best = Math.max(best, e.getKey());
        }
        return best;
    }

    private static void testSpaceOddityUnit() {
        check(Problems.spaceOddity(new Long[]{1L,5L,2L,4L,6L,5L,1L,5L,5L,2L,5L}) == 6L, "SO ex1");
        check(Problems.spaceOddity(new Long[]{1L,1L,5L,5L,2L,3L,2L,3L}) == -1L, "SO ex2");
        check(Problems.spaceOddity(new Long[]{9L,9L,1L,5L,1L,9L,1L,9L}) == 5L, "SO ex3");
        long big = (1L << 32) - 1L;
        check(Problems.spaceOddity(new Long[]{big, big, big}) == big, "SO big");
    }

    private static void testSpaceOddityFuzz() {
        Random r = new Random(99);
        for (int rep = 0; rep < 400; rep++) {
            int n = r.nextInt(1, 400);
            Long[] A = new Long[n];
            for (int i = 0; i < n; i++) A[i] = (long) r.nextInt(0, 1 << 20);
            long exp = spaceOddityOracle(A);
            long got = Problems.spaceOddity(A);
            if (exp != got) {
                throw new AssertionError("SpaceOddity mismatch exp=" + exp + " got=" + got);
            }
        }
    }

    /* =========================================================
     * Frea-k-y Numbers
     * ========================================================= */

    // k-freaky iff base-k expansion uses only digits {0,1}
    private static boolean isKFreaky(long x, long k) {
        if (k <= 1) return (x == 0 || x == 1); // degenerate guard
        if (x == 0) return true;
        long t = x;
        while (t > 0) {
            long d = t % k;
            if (d != 0 && d != 1) return false;
            t /= k;
        }
        return true;
    }

    private static long kFreakyOracle(long m, long n, long k) {
        long lo = Math.min(m, n), hi = Math.max(m, n), cnt = 0;
        for (long v = lo; v <= hi; v++) if (isKFreaky(v, k)) cnt++;
        return cnt;
    }

    private static void testFreakyUnit() {
        check(isKFreaky(17, 4), "17 4-freaky");
        check(isKFreaky(128, 2), "128 2-freaky");
        check(isKFreaky(11, 10), "11 10-freaky");
        check(Problems.freakyNumbers(17, 17, 4) == 1, "kFreaky single");
        check(Problems.freakyNumbers(0, 50, 3) == kFreakyOracle(0, 50, 3), "kFreaky base3 small");
    }

    private static void testFreakyFuzz() {
        Random r = new Random(2025);
        for (int rep = 0; rep < 200; rep++) {
            long m = r.nextInt(0, 500);
            long n = r.nextInt(0, 500);
            long k = r.nextInt(2, 12);
            long exp = kFreakyOracle(m, n, k);
            long got = Problems.freakyNumbers(m, n, k);
            if (exp != got) {
                throw new AssertionError("freakyNumbers mismatch exp=" + exp + " got=" + got +
                        " for m=" + m + " n=" + n + " k=" + k);
            }
        }
    }

    /* =========================================================
     * Life is Sweet
     * ========================================================= */

    private static final long INF = (1L << 60);

    // Oracle for small boards via DP + memo
    private static long lifeIsSweetOracle(int a, int b, int k) {
        if (a < 0 || b < 0) return INF;
        Map<String, Long> memo = new HashMap<>();
        return lisRec(a, b, k, memo);
    }

    private static String key(int a, int b, int k) { return a + ":" + b + ":" + k; }

    private static long lisRec(int a, int b, int k, Map<String, Long> memo) {
        if (k < 0 || k > a * b) return INF;
        if (k == 0 || k == a * b) return 0;
        if (a > b) { int t = a; a = b; b = t; }
        String K = key(a, b, k);
        if (memo.containsKey(K)) return memo.get(K);

        long best = INF;

        // vertical cuts (split a into x and a-x), cost = b^2
        for (int x = 1; x <= a - 1; x++) {
            long base = 1L * b * b;
            for (int s = 0; s <= k; s++) {
                long c1 = lisRec(x, b, s, memo);
                if (c1 == INF) continue;
                long c2 = lisRec(a - x, b, k - s, memo);
                if (c2 == INF) continue;
                best = Math.min(best, base + c1 + c2);
            }
        }
        // horizontal cuts (split b into y and b-y), cost = a^2
        for (int y = 1; y <= b - 1; y++) {
            long base = 1L * a * a;
            for (int s = 0; s <= k; s++) {
                long c1 = lisRec(a, y, s, memo);
                if (c1 == INF) continue;
                long c2 = lisRec(a, b - y, k - s, memo);
                if (c2 == INF) continue;
                best = Math.min(best, base + c1 + c2);
            }
        }

        memo.put(K, best);
        return best;
    }

    private static void testLifeIsSweetUnit() {
        // trivial
        check(Problems.lifeIsSweet(0, 0, 0) == 0, "lis 0x0 k=0");
        check(Problems.lifeIsSweet(1, 1, 0) == 0, "lis 1x1 k=0");
        check(Problems.lifeIsSweet(1, 1, 1) == 0, "lis 1x1 k=1");

        // small boards vs oracle
        int[][] cases = {
                {2,2,1},{2,2,2},{2,2,3},
                {2,3,1},{2,3,2},{2,3,3},{2,3,4},{2,3,5},
                {3,3,1},{3,3,2},{3,3,3},{3,3,4},{3,3,5},{3,3,6},{3,3,7},{3,3,8}
        };
        for (int[] c : cases) {
            int a=c[0], b=c[1], k=c[2];
            long exp = lifeIsSweetOracle(a, b, k);
            long got = Problems.lifeIsSweet(a, b, k);
            if (exp != got) {
                throw new AssertionError("LifeIsSweet mismatch a=" + a + " b=" + b + " k=" + k +
                        " exp=" + exp + " got=" + got);
            }
        }
    }

    private static void testLifeIsSweetFuzzSmall() {
        Random r = new Random(77);
        for (int rep = 0; rep < 80; rep++) {
            int a = r.nextInt(1, 5);
            int b = r.nextInt(1, 5);
            int area = a * b;
            int k = r.nextInt(0, area + 1);
            long exp = lifeIsSweetOracle(a, b, k);
            long got = Problems.lifeIsSweet(a, b, k);
            if (exp != got) {
                throw new AssertionError("LifeIsSweet fuzz mismatch a=" + a + " b=" + b + " k=" + k +
                        " exp=" + exp + " got=" + got);
            }
        }
    }

    /* =========================================================
     * Main
     * ========================================================= */

    public static void main(String[] args) {
        System.out.println("Testing Problems…");

        // RLE
        testShortRunsUnit();
        testShortRunsFuzz();

        // Arithmetic Rules
        testArithmeticRulesUnit();
        testArithmeticRulesFuzz();

        // SQRT Happens
        testSqrtUnit();
        testSqrtFuzz();

        // Space Oddity
        testSpaceOddityUnit();
        testSpaceOddityFuzz();

        // Frea-k-y Numbers
        testFreakyUnit();
        testFreakyFuzz();

        // Life is Sweet
        testLifeIsSweetUnit();
        testLifeIsSweetFuzzSmall();

        System.out.println("Success! All tests passed.");
    }
}
