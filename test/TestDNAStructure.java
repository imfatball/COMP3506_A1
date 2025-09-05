/**
 * Supplied by the COMP3506/7505 teaching team, Semester 2, 2025.
 */

// @edu:student-assignment
// Save as: TestDNAStructure.java
// Run with assertions enabled:  java -ea TestDNAStructure
import uq.comp3506.a1.DNAStructure;
// @edu:student-assignment
// Save as: TestDNAStructure.java
// Run with:  javac DNAStructure.java TestDNAStructure.java && java -ea TestDNAStructure

import java.util.Random;

public class TestDNAStructure {

    // ---------- tiny assert helpers ----------
    private static void assertEq(int a, int b, String msg) {
        if (a != b) throw new AssertionError(msg + " | expected=" + b + " actual=" + a);
    }
    private static void assertEq(boolean a, boolean b, String msg) {
        if (a != b) throw new AssertionError(msg + " | expected=" + b + " actual=" + a);
    }
    private static void assertEq(String a, String b, String msg) {
        if (!a.equals(b)) throw new AssertionError(msg + " | expected=" + b + " actual=" + a);
    }

    // ---------- deterministic tests ----------

    private static void testBasicsAndStringify() {
        int w = 5; // w is positive
        DNAStructure dna = new DNAStructure(w);
        assertEq(dna.isFull(), false, "empty not full");
        assertEq(dna.stringify(), "", "empty stringify");

        dna.slide('A'); // A
        dna.slide('C'); // AC
        dna.slide('G'); // ACG
        assertEq(dna.stringify(), "ACG", "append into ring");
        assertEq(dna.count('A'), 1, "count A=1");
        assertEq(dna.count('T'), 0, "count T=0");

        dna.slide('T'); // ACGT
        dna.slide('A'); // ACGTA (full)
        assertEq(dna.isFull(), true, "now full");
        assertEq(dna.stringify(), "ACGTA", "full stringify");

        // Overwrite head on next slide:
        dna.slide('T'); // CGTAT (A overwritten)
        assertEq(dna.stringify(), "CGTAT", "head moved after overwrite");
        assertEq(dna.count('A'), 1, "A count after one evict");
        assertEq(dna.count('C'), 1, "C count still 1");
        assertEq(dna.count('T'), 2, "T count 2");
    }

    private static void testCountRepeatsWithinKRange() {
        // Example from spec: CCTATAGGTATACATA, countRepeats(3) = 2 (TAT, ATA)
        String s = "CCTATAGGTATACATA";
        DNAStructure dna = new DNAStructure(s.length());
        for (int i = 0; i < s.length(); i++) dna.slide(s.charAt(i));
        // k=3 is within [2,13]
        assertEq(dna.countRepeats(3), 2, "countRepeats k=3 example");

        // A case with obvious repeats for a few k in range
        String t = "AAAAACCCCCAAAAACCCCC";
        dna = new DNAStructure(t.length());
        for (int i = 0; i < t.length(); i++) dna.slide(t.charAt(i));
        for (int k = 2; k <= 6; k++) {
            int r = dna.countRepeats(k);
            if (r <= 0) throw new AssertionError("Expected repeats for k=" + k + " in '" + t + "', got " + r);
        }

        // k near upper bound but <= size
        String u = "ACGTACGTACGTACGTACGT";
        dna = new DNAStructure(u.length());
        for (int i = 0; i < u.length(); i++) dna.slide(u.charAt(i));
        int k = Math.min(13, u.length()); // ensure within [2,13] and ≤ size
        int rr = dna.countRepeats(k);
        // If k == length, repeats == 0 unless the whole string occurs at least twice (it doesn't)
        if (k == u.length()) assertEq(rr, 0, "k == size → no repeats");
    }

    private static void testHasPalindromeEvenK() {
        // Even-length DNA palindrome: ACCTAGGT (from spec)
        String palEven = "ACCTAGGT";
        DNAStructure dna = new DNAStructure(palEven.length());
        for (int i = 0; i < palEven.length(); i++) dna.slide(palEven.charAt(i));
        assertEq(dna.hasPalindrome(8), true, "whole even palindrome found");

        // Embedded even palindrome
        String w = "GGACCTAGGTT";
        dna = new DNAStructure(w.length());
        for (int i = 0; i < w.length(); i++) dna.slide(w.charAt(i));
        assertEq(dna.hasPalindrome(8), true, "embedded even palindrome found");

        // Typical random string is unlikely to have even-length palindromes for larger k (but we don't assert false).
        // Just sanity: small even k should sometimes be true in crafted sequences.
        String z = "TTAA"; // reverse complement palindrome of length 4
        dna = new DNAStructure(z.length());
        for (int i = 0; i < z.length(); i++) dna.slide(z.charAt(i));
        assertEq(dna.hasPalindrome(4), true, "TTAA is an even DNA palindrome");
    }

    private static void testKBounds() {
        String s = "ACGTACGT";
        DNAStructure dna = new DNAStructure(s.length());
        for (int i = 0; i < s.length(); i++) dna.slide(s.charAt(i));

        // k in [2,13], but also must be ≤ size for meaningful queries
        assertEq(dna.countRepeats(2) >= 0, true, "k=2 legit");
        assertEq(dna.countRepeats(Math.min(13, s.length())) >= 0, true, "k up to 13");

        // hasPalindrome only checked when 2 ≤ k ≤ size (we stay in range)
        assertEq(dna.hasPalindrome(2) || !dna.hasPalindrome(2), true, "k=2 call ok");
    }

    // ---------- oracles for fuzz ----------

    private static final char[] ALPH = new char[]{'A','C','G','T'};

    private static int oracleCount(char[] buf, int head, int size, int cap, char c) {
        int cnt = 0;
        for (int i = 0; i < size; i++) if (buf[(head + i) % cap] == c) cnt++;
        return cnt;
    }

    private static int oracleCountRepeats(char[] buf, int head, int size, int cap, int k) {
        if (k < 2 || k > size) return 0;
        boolean[] already = new boolean[size];
        int repeats = 0;
        for (int i = 0; i <= size - k; i++) {
            if (already[i]) continue;
            boolean dup = false;
            for (int j = i + 1; j <= size - k; j++) {
                boolean same = true;
                for (int t = 0; t < k; t++) {
                    if (buf[(head + i + t) % cap] != buf[(head + j + t) % cap]) { same = false; break; }
                }
                if (same) { dup = true; already[j] = true; }
            }
            if (dup) repeats++;
        }
        return repeats;
    }

    private static char comp(char c) {
        switch (c) {
            case 'A': return 'T';
            case 'T': return 'A';
            case 'C': return 'G';
            case 'G': return 'C';
        }
        return c;
    }

    private static boolean oracleHasPalindrome(char[] buf, int head, int size, int cap, int k) {
        if (k < 2 || k > size) return false;
        for (int i = 0; i <= size - k; i++) {
            boolean pal = true;
            for (int j = 0; j < k; j++) {
                char left  = buf[(head + i + j) % cap];
                char right = buf[(head + i + k - 1 - j) % cap];
                if (comp(left) != right) { pal = false; break; }
            }
            if (pal) return true;
        }
        return false;
    }

    // ---------- randomized fuzz (k in [2,13], w positive) ----------

    private static void randomizedFuzz(long seed, int window, int ops) {
        if (window <= 0) throw new IllegalArgumentException("window must be positive for this test");
        System.out.println("Fuzz seed=" + seed + " window=" + window + " ops=" + ops);
        Random rnd = new Random(seed);

        DNAStructure dna = new DNAStructure(window);

        char[] buf = new char[window]; // oracle mirror buffer
        int head = 0;
        int size = 0;

        for (int step = 0; step < ops; step++) {
            int choice = rnd.nextInt(7);
            if (choice <= 3) {
                // slide a random base
                char c = ALPH[rnd.nextInt(ALPH.length)];
                dna.slide(c);

                // oracle window update
                if (size < window) {
                    buf[(head + size) % window] = c;
                    size++;
                } else {
                    buf[head] = c;
                    head = (head + 1) % window;
                }
            } else if (choice == 4) {
                // count(c)
                char c = ALPH[rnd.nextInt(ALPH.length)];
                int got = dna.count(c);
                int exp = oracleCount(buf, head, size, window, c);
                if (got != exp) throw new RuntimeException("count(" + c + ") mismatch @step=" + step + " exp=" + exp + " got=" + got);
            } else if (choice == 5) {
                // countRepeats(k) with k in [2,13] and ≤ size
                if (size >= 2) {
                    int hi = Math.min(13, size);
                    int k = 2 + rnd.nextInt(Math.max(1, hi - 1)); // 2..hi
                    int got = dna.countRepeats(k);
                    int exp = oracleCountRepeats(buf, head, size, window, k);
                    if (got != exp)
                        throw new RuntimeException("countRepeats(" + k + ") mismatch @step=" + step + " exp=" + exp + " got=" + got);
                }
            } else {
                // hasPalindrome(k) with k in [2,13] and ≤ size, prefer even k
                if (size >= 2) {
                    int hi = Math.min(13, size);
                    int k;
                    if (hi >= 4) {
                        // pick an even k in [2,hi]
                        int evens = (hi / 2);
                        k = 2 * (1 + rnd.nextInt(evens)); // 2,4,6,...
                        if (k > hi) k = hi - (hi % 2);     // clamp even
                        if (k < 2)  k = 2;
                    } else {
                        k = 2; // smallest allowed
                    }
                    boolean got = dna.hasPalindrome(k);
                    boolean exp = oracleHasPalindrome(buf, head, size, window, k);
                    if (got != exp)
                        throw new RuntimeException("hasPalindrome(" + k + ") mismatch @step=" + step + " exp=" + exp + " got=" + got);
                }
            }
        }
    }

    // ---------- runner ----------

    public static void main(String[] args) {
        System.out.println("Testing DNAStructure (w>0, k in [2,13])…");

        testBasicsAndStringify();
        testCountRepeatsWithinKRange();
        testHasPalindromeEvenK();
        testKBounds();

        randomizedFuzz(123456789L,  8,  6000);
        randomizedFuzz(987654321L, 16,  9000);
        randomizedFuzz(System.currentTimeMillis(), 32, 15000);

        System.out.println("All tests passed ✅");
    }
}

