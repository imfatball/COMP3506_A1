import java.util.*;
import uq.comp3506.a1.structures.BitVector;

/**
 * Run with:  java -ea TestBitVector
 */
public class TestBitVector {

    /* ===========================
     * Oracle (BitSet wrapper)
     * =========================== */

    static final class Oracle {
        final int n;     // number of active bits
        final BitSet bs;

        Oracle(int n) {
            if (n < 0) throw new IllegalArgumentException("n < 0");
            this.n = n;
            this.bs = new BitSet(n);
        }

        boolean get(int i) {
            checkIndex(i);
            return bs.get(i);
        }
        void set(int i) {
            checkIndex(i);
            bs.set(i);
        }
        void unset(int i) {
            checkIndex(i);
            bs.clear(i);
        }
        void complement() {
            bs.flip(0, n);
        }
        void shift(int dist) {
            if (n == 0 || dist == 0) return;
            if (dist >= n || dist <= -n) {
                bs.clear();
                return;
            }
            BitSet dst = new BitSet(n);
            if (dist > 0) {
                forEachSetBit(bs, i -> {
                    int j = i + dist;
                    if (j >= 0 && j < n) dst.set(j);
                });
            } else {
                int k = -dist;
                forEachSetBit(bs, i -> {
                    int j = i - k;
                    if (j >= 0 && j < n) dst.set(j);
                });
            }
            bs.clear();
            bs.or(dst);
        }
        void rotate(int dist) {
            if (n == 0) return;
            int d = mod(dist, n);
            if (d == 0) return;
            BitSet dst = new BitSet(n);
            forEachSetBit(bs, i -> dst.set((i + d) % n));
            bs.clear();
            bs.or(dst);
        }
        int popcount() { return bs.cardinality(); }

        private void checkIndex(int i) {
            if (i < 0 || i >= n) throw new IndexOutOfBoundsException("ix=" + i + " size=" + n);
        }
        private static void forEachSetBit(BitSet b, java.util.function.IntConsumer f) {
            int i = b.nextSetBit(0);
            while (i >= 0) {
                f.accept(i);
                i = b.nextSetBit(i + 1);
            }
        }
        private static int mod(int a, int m) {
            int r = a % m;
            return (r < 0) ? r + m : r;
        }
    }

    /* ===========================
     * Helpers
     * =========================== */

    private static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    private static boolean[] snapshot(BitVector bv) {
        long n = bv.size();
        boolean[] out = new boolean[(int) n];
        for (int i = 0; i < n; i++) out[i] = bv.get(i);
        return out;
    }

    private static boolean[] snapshot(Oracle o) {
        boolean[] out = new boolean[o.n];
        for (int i = 0; i < o.n; i++) out[i] = o.get(i);
        return out;
    }

    private static void assertSameContent(Oracle o, BitVector bv) {
        check(bv.size() == o.n, "size mismatch: bv=" + bv.size() + " oracle=" + o.n);
        boolean[] a = snapshot(bv);
        boolean[] b = snapshot(o);
        if (a.length != b.length) throw new AssertionError("length mismatch");
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                throw new AssertionError("content mismatch at i=" + i + " expected=" + b[i] + " actual=" + a[i]);
            }
        }
    }

    /* ===========================
     * Deterministic unit tests
     * =========================== */

    private static void testConstructionAndCapacity() {
        int[] sizes = {0, 1, 5, 63, 64, 65, 127, 128, 129, 1000};
        for (int n : sizes) {
            BitVector bv = new BitVector(n);
            long cap = bv.capacity();
            check(bv.size() == n, "size wrong for n=" + n);
            check(cap % 64 == 0, "capacity not multiple of 64");
            check(cap >= n, "capacity < size");
            long expCap = ((n + 63L) / 64L) * 64L;
            check(cap == expCap, "unexpected capacity for n=" + n + " cap=" + cap + " expected=" + expCap);
        }
    }

    private static void testBounds() {
        BitVector bv = new BitVector(10);
        boolean threw;
        threw = false; try { bv.get(-1); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "get(-1) should throw");
        threw = false; try { bv.get(10); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "get(size) should throw");

        threw = false; try { bv.set(-1); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "set(-1) should throw");
        threw = false; try { bv.set(10); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "set(size) should throw");

        threw = false; try { bv.unset(-1); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "unset(-1) should throw");
        threw = false; try { bv.unset(10); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "unset(size) should throw");
    }

    private static void testSetUnsetGet() {
        BitVector bv = new BitVector(8);
        Oracle o = new Oracle(8);
        for (int i = 0; i < 8; i++) check(!bv.get(i), "initial should be 0");
        bv.set(0); o.set(0);
        bv.set(3); o.set(3);
        bv.set(7); o.set(7);
        assertSameContent(o, bv);

        bv.unset(3); o.unset(3);
        assertSameContent(o, bv);
    }

    private static void testComplementTailMasking() {
        int n = 70; // non-multiple of 64
        BitVector bv = new BitVector(n);
        Oracle o = new Oracle(n);
        int[] bits = {0, 1, 2, 63, 64, 65, 69};
        for (int b : bits) { bv.set(b); o.set(b); }
        assertSameContent(o, bv);

        bv.complement(); o.complement();
        assertSameContent(o, bv);
    }

    private static void testShiftSmall() {
        int n = 10;
        BitVector bv = new BitVector(n);
        Oracle o = new Oracle(n);

        int[] ones = {0, 2, 3, 7, 9}; // [1 0 1 1 0 0 0 1 0 1]
        for (int b : ones) { bv.set(b); o.set(b); }
        assertSameContent(o, bv);

        bv.shift(2); o.shift(2);   // left
        assertSameContent(o, bv);

        bv.shift(-3); o.shift(-3); // right
        assertSameContent(o, bv);

        bv.shift(100); o.shift(100); // all clear
        assertSameContent(o, bv);
    }

    private static void testRotateSmall() {
        int n = 5;
        BitVector bv = new BitVector(n);
        Oracle o = new Oracle(n);

        int[] ones = {0, 2, 3, 4};   // [1 0 1 1 1]
        for (int b : ones) { bv.set(b); o.set(b); }
        assertSameContent(o, bv);

        bv.rotate(2); o.rotate(2);
        assertSameContent(o, bv);

        bv.rotate(-1); o.rotate(-1);
        assertSameContent(o, bv);

        bv.rotate(5); o.rotate(5); // no-op
        assertSameContent(o, bv);

        bv.rotate(5 * 100 + 3); o.rotate(5 * 100 + 3);
        assertSameContent(o, bv);
    }

    private static void testEdgeSizes() {
        // size 0
        BitVector z = new BitVector(0);
        check(z.size() == 0, "size 0 expected");
        check(z.capacity() == 0, "cap 0 expected");

        // get should throw
        boolean threw = false; try { z.get(0); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "get(0) should throw on size 0");

        // non-index methods are no-ops:
        z.shift(10);
        z.rotate(10);
        z.complement();

        // size 1
        BitVector one = new BitVector(1);
        Oracle o = new Oracle(1);
        one.set(0); o.set(0);
        assertSameContent(o, one);
        one.rotate(1234567); o.rotate(1234567);
        assertSameContent(o, one);
        one.shift(1); o.shift(1); // clears
        assertSameContent(o, one);
        one.complement(); o.complement();
        assertSameContent(o, one);
    }

    /* ===========================
     * Fuzz testing
     * =========================== */

    private static void randomizedFuzz(long seed, int ops, int size) {
        Random r = new Random(seed);
        BitVector bv = new BitVector(size);
        Oracle o = new Oracle(size);

        // rolling op log of last 32 operations
        final String[] ring = new String[32];
        final int[] ringPos = {0};
        java.util.function.Consumer<String> log = s -> ring[ringPos[0]++ & 31] = s;

        Runnable dumpLog = () -> {
            System.err.println("Recent ops (oldest→newest):");
            for (int k = 0; k < 32; k++) {
                String s = ring[(ringPos[0] + k) & 31];
                if (s != null) System.err.println("  " + s);
            }
            System.err.println("size=" + size + " oraclePop=" + o.popcount());
            try {
                boolean bvHead = size > 0 && bv.get(0);
                boolean oHead = size > 0 && o.get(0);
                System.err.println("head: bv=" + bvHead + " oracle=" + oHead);
            } catch (Throwable ignore) {}
        };

        for (int step = 0; step < ops; step++) {
            int op = r.nextInt(100);
            try {
                if (op < 45) {
                    // set / unset / get
                    if (size == 0) {
                        // only test the throwing behavior for get
                        log.accept("get(0) // expect throw");
                        boolean threw = false;
                        try { bv.get(0); } catch (IndexOutOfBoundsException e) { threw = true; }
                        check(threw, "get on size 0 should throw");
                    } else {
                        int ix = r.nextInt(size);
                        int which = r.nextInt(3);
                        if (which == 0) {
                            log.accept("set(" + ix + ")");
                            bv.set(ix); o.set(ix);
                        } else if (which == 1) {
                            log.accept("unset(" + ix + ")");
                            bv.unset(ix); o.unset(ix);
                        } else {
                            log.accept("get(" + ix + ")");
                            boolean a = bv.get(ix);
                            boolean b = o.get(ix);
                            check(a == b, "get mismatch at " + ix);
                        }
                    }
                } else if (op < 65) {
                    // complement
                    log.accept("complement()");
                    bv.complement(); o.complement();
                } else if (op < 85) {
                    // shift
                    int dist = size == 0 ? 0
                            : (r.nextBoolean() ? r.nextInt(size + 128) : -r.nextInt(size + 128));
                    log.accept("shift(" + dist + ")");
                    bv.shift(dist); o.shift(dist);
                } else {
                    // rotate
                    int dist = size == 0 ? 0
                            : (r.nextBoolean() ? r.nextInt(size + 128) : -r.nextInt(size + 128));
                    log.accept("rotate(" + dist + ")");
                    bv.rotate(dist); o.rotate(dist);
                }
            } catch (AssertionError | RuntimeException ex) {
                dumpLog.run();
                throw new RuntimeException(
                        "Fuzz failure at step " + step + " size=" + size + " seed=" + seed + ": " + ex.getMessage(), ex);
            }

            // periodic full check
            if ((step & 255) == 0) {
                assertSameContent(o, bv);
            }
        }

        // final check
        assertSameContent(o, bv);
    }

    /* ===========================
     * Main
     * =========================== */

    public static void main(String[] args) {
        System.out.println("Testing BitVector…");

        testConstructionAndCapacity();
        testBounds();
        testSetUnsetGet();
        testComplementTailMasking();
        testShiftSmall();
        testRotateSmall();
        testEdgeSizes();

        long seed1 = System.currentTimeMillis();
        System.out.println("Fuzz A seed=" + seed1);
        randomizedFuzz(seed1, 50_000, 0);
        randomizedFuzz(seed1, 50_000, 1);
        randomizedFuzz(seed1, 50_000, 5);
        randomizedFuzz(seed1, 50_000, 63);
        randomizedFuzz(seed1, 50_000, 64);
        randomizedFuzz(seed1, 50_000, 65);
        randomizedFuzz(seed1, 50_000, 97);
        randomizedFuzz(seed1, 50_000, 128);
        randomizedFuzz(seed1, 50_000, 130);

        long seed2 = 123456789L; // reproducible
        System.out.println("Fuzz B seed=" + seed2);
        randomizedFuzz(seed2, 100_000, 70);
        randomizedFuzz(seed2, 100_000, 128);
        randomizedFuzz(seed2, 100_000, 97);

        System.out.println("Success! All tests passed.");
    }
}
