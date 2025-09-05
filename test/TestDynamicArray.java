/**
 * Supplied by the COMP3506/7505 teaching team, Semester 2, 2025.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import uq.comp3506.a1.structures.DynamicArray;

public class TestDynamicArray {

    /* ----------------------------- Helpers ----------------------------- */

    private static boolean eq(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    private static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }


    private static <T extends Comparable<T>>
    void assertSameContent(ArrayList<T> oracle, DynamicArray<T> da) {
        if (oracle.size() != da.size()) {
            throw new AssertionError("size mismatch: oracle=" + oracle.size() + " da=" + da.size());
        }
        for (int i = 0; i < oracle.size(); i++) {
            T a = oracle.get(i);
            T b = da.get(i);
            if (!java.util.Objects.equals(a, b)) {
                throw new AssertionError("content mismatch at i=" + i + " expected=" + a + " actual=" + b);
            }
        }
    }


    /* ----------------------------- Tiny type for sort stability ----------------------------- */

    /** Compares by key only; id preserves insertion order for stability checks. */
    static final class Pair implements Comparable<Pair> {
        final int key;
        final int id;
        Pair(int key, int id) { this.key = key; this.id = id; }
        @Override public int compareTo(Pair o) {
            return Integer.compare(this.key, o.key);
        }
        @Override public String toString() { return "(" + key + "," + id + ")"; }
        @Override public boolean equals(Object x) {
            if (!(x instanceof Pair)) return false;
            Pair p = (Pair) x;
            return key == p.key && id == p.id;
        }
        @Override public int hashCode() { return key * 31 + id; }
    }

    /* ----------------------------- Deterministic Unit Tests ----------------------------- */

    private static void testBasics() {
        DynamicArray<Integer> da = new DynamicArray<>();
        check(da.size() == 0, "size should be 0");
        check(da.isEmpty(), "isEmpty should be true");

        boolean threw;
        threw = false; try { da.get(0); check(false, "get(0) should throw"); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "expected get(0) throw");
        threw = false; try { da.set(0, 1); check(false, "set(0) should throw"); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "expected set(0) throw");
        threw = false; try { da.remove(0); check(false, "remove(0) should throw"); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "expected remove(0) throw");
    }

    private static void testAppendPrependGrowth() {
        DynamicArray<Integer> da = new DynamicArray<>();
        ArrayList<Integer> o = new ArrayList<>();

        // Append a lot (forces doubling)
        for (int i = 0; i < 2000; i++) { da.append(i); o.add(i); }
        assertSameContent(o, da);

        // Prepend a lot (exercises circular start wrap)
        for (int i = 0; i < 500; i++) { da.prepend(-i-1); o.add(0, -i-1); }
        assertSameContent(o, da);

        // Add at ends via add(idx, x)
        da.add(0, 9999); o.add(0, 9999);
        da.add(da.size(), -9999); o.add(o.size(), -9999);
        assertSameContent(o, da);
    }

    private static void testAddMiddleAndBounds() {
        DynamicArray<Integer> da = new DynamicArray<>();
        ArrayList<Integer> o = new ArrayList<>();
        for (int i = 0; i < 20; i++) { da.append(i); o.add(i); }

        // insertions in the middle (hit both prefix/suffix shift branches)
        da.add(10, 100); o.add(10, 100);
        da.add(3, 200);  o.add(3, 200);
        da.add(15, 300); o.add(15, 300);
        assertSameContent(o, da);

        // bounds
        boolean threw;
        threw = false; try { da.add(-1, 7); check(false, "add(-1,…) should throw"); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "expected add(-1) throw");
        threw = false; try { da.add(da.size() + 1, 7); check(false, "add(size+1,…) should throw"); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "expected add(size+1) throw");
    }

    private static void testSetGetRemoveVariants() {
        DynamicArray<Integer> da = new DynamicArray<>();
        ArrayList<Integer> o = new ArrayList<>();
        for (int i = 0; i < 30; i++) { da.append(i); o.add(i); }

        // set
        check(eq(da.set(0, 1000), 0), "set head return mismatch");
        o.set(0, 1000);
        check(eq(da.set(10, 2000), 10), "set mid return mismatch");
        o.set(10, 2000);
        check(eq(da.set(da.size()-1, 3000), 29), "set tail return mismatch");
        o.set(o.size()-1, 3000);
        assertSameContent(o, da);

        // remove head / mid / tail
        check(eq(da.remove(0), 1000), "remove head ret mismatch");  o.remove(0);
        check(eq(da.remove(5), 6),     "remove mid ret mismatch");   o.remove(5);
        check(eq(da.remove(da.size()-1), 3000), "remove tail ret mismatch"); o.remove(o.size()-1);
        assertSameContent(o, da);

        // bounds
        boolean threw;
        threw = false; try { da.get(da.size()); check(false, "get(size) should throw"); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "expected get(size) throw");
        threw = false; try { da.set(da.size(), 0); check(false, "set(size,…) should throw"); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "expected set(size) throw");
        threw = false; try { da.remove(da.size()); check(false, "remove(size) should throw"); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "expected remove(size) throw");
    }

    private static void testRemoveFirstWithNulls() {
        DynamicArray<Integer> da = new DynamicArray<>();
        ArrayList<Integer> o = new ArrayList<>();
        // [null, 1, null, 2, 1]
        da.append(null); o.add(null);
        da.append(1);    o.add(1);
        da.append(null); o.add(null);
        da.append(2);    o.add(2);
        da.append(1);    o.add(1);

        check(da.removeFirst(null) == o.remove(null), "removeFirst(null) result mismatch");
        assertSameContent(o, da);

        check(da.removeFirst(42) == o.remove((Integer)42), "removeFirst(42) result mismatch");
        assertSameContent(o, da);

        check(da.removeFirst(1) == o.remove((Integer)1), "removeFirst(1) result mismatch");
        assertSameContent(o, da);
    }

    private static void testClearAndReuse() {
        DynamicArray<Integer> da = new DynamicArray<>();
        ArrayList<Integer> o = new ArrayList<>();
        for (int i = 0; i < 200; i++) { da.prepend(i); o.add(0, i); }
        assertSameContent(o, da);
        da.clear(); o.clear();
        check(da.size() == 0 && o.size() == 0, "clear size mismatch");

        // reuse after clear
        for (int i = 0; i < 25; i++) { da.append(i * 3); o.add(i * 3); }
        assertSameContent(o, da);
    }

    private static void testSortBasicAndStability() {
        // basic ints
        DynamicArray<Integer> da = new DynamicArray<>();
        ArrayList<Integer> o = new ArrayList<>();

        // reverse order + dups
        for (int i = 30; i >= 0; i--) { da.append(i / 2); o.add(i / 2); }
        da.sort(); Collections.sort(o);
        assertSameContent(o, da);

        // already sorted
        da.clear(); o.clear();
        for (int i = 0; i < 40; i++) { da.append(i / 3); o.add(i / 3); }
        da.sort(); Collections.sort(o);
        assertSameContent(o, da);

        // stability using Pair (compare by key only, preserve id order)
        DynamicArray<Pair> dp = new DynamicArray<>();
        ArrayList<Pair> op = new ArrayList<>();
        int id = 0;
        Pair[] input = new Pair[] {
                new Pair(2, id++), new Pair(1, id++), new Pair(2, id++), new Pair(1, id++),
                new Pair(3, id++), new Pair(2, id++), new Pair(1, id++), new Pair(3, id++)
        };
        for (Pair p : input) { dp.append(p); op.add(p); }
        dp.sort();
        // stable sort in Java: Collections.sort is stable
        op.sort(Comparator.comparingInt(a -> a.key));
        // verify sorted by key and, within equal keys, by original id order
        check(dp.size() == op.size(), "pair size mismatch");
        for (int i = 0; i < op.size(); i++) {
            Pair a = op.get(i);
            Pair b = dp.get(i);
            if (!a.equals(b)) {
                throw new AssertionError("stability/content mismatch at i=" + i + " expected=" + a + " actual=" + b);
            }
        }

        // null policy: if your sort() throws on nulls, confirm it
        DynamicArray<Integer> dz = new DynamicArray<>();
        dz.append(3); dz.append(null); dz.append(1);
        boolean threw = false;
        try { dz.sort(); } catch (NullPointerException e) { threw = true; }
        check(threw, "sort should throw on null elements (per policy)");
    }

    /* ----------------------------- Randomized Fuzz Against ArrayList ----------------------------- */

    private static void randomizedFuzz(long seed, int ops) {
        Random r = new Random(seed);
        DynamicArray<Integer> da = new DynamicArray<>();
        ArrayList<Integer> o = new ArrayList<>();

        // --- rolling log of the last 32 operations ---
        final String[] ring = new String[32];
        final int[] ringPos = {0}; // mutable wrapper so lambda compiles
        java.util.function.Consumer<String> log = s -> ring[ringPos[0]++ & 31] = s;

        Runnable dumpLog = () -> {
            System.err.println("Recent ops before failure (oldest→newest):");
            for (int k = 0; k < 32; k++) {
                String s = ring[(ringPos[0] + k) & 31];
                if (s != null) System.err.println("  " + s);
            }
            Integer oHead = o.isEmpty() ? null : o.get(0);
            Integer daHead = null;
            try { daHead = da.size() > 0 ? da.get(0) : null; } catch (Throwable ignored) {}
            System.err.println("Oracle size=" + o.size() + " head=" + oHead);
            System.err.println("DA     size=" + da.size() + " head=" + daHead);
        };

        for (int step = 0; step < ops; step++) {
            int op = r.nextInt(100);
            try {
                if (op < 22) { // append
                    Integer v = r.nextBoolean() ? null : r.nextInt(11) - 5;
                    log.accept("append(" + v + ")");
                    da.append(v); o.add(v);
                } else if (op < 40) { // prepend
                    Integer v = r.nextBoolean() ? null : r.nextInt(11) - 5;
                    log.accept("prepend(" + v + ")");
                    o.add(0, v);
                    da.prepend(v);
                } else if (op < 58) { // add at index (including end)
                    Integer v = r.nextBoolean() ? null : r.nextInt(21) - 10;
                    int idx = o.isEmpty() ? 0 : r.nextInt(o.size() + 1);
                    log.accept("add(" + idx + ", " + v + ")");
                    o.add(idx, v);
                    da.add(idx, v);
                } else if (op < 72) { // set
                    if (!o.isEmpty()) {
                        int idx = r.nextInt(o.size());
                        Integer v = r.nextBoolean() ? null : r.nextInt(31) - 15;
                        log.accept("set(" + idx + ", " + v + ")");
                        Integer exp = o.set(idx, v);
                        Integer got = da.set(idx, v);
                        check(java.util.Objects.equals(exp, got), "set return mismatch");
                    } else {
                        log.accept("set(0, <skipped empty>)");
                    }
                } else if (op < 86) { // remove at index
                    if (!o.isEmpty()) {
                        int idx = r.nextInt(o.size());
                        log.accept("remove(" + idx + ")");
                        Integer exp = o.remove(idx);
                        Integer got = da.remove(idx);
                        check(java.util.Objects.equals(exp, got), "remove return mismatch");
                    } else {
                        log.accept("remove(0) // expect throw");
                        boolean threw = false;
                        try { da.remove(0); } catch (IndexOutOfBoundsException e) { threw = true; }
                        check(threw, "remove on empty should throw");
                    }
                } else if (op < 94) { // removeFirst by value
                    Integer target = r.nextBoolean() ? null : r.nextInt(11) - 5;
                    log.accept("removeFirst(" + target + ")");
                    boolean exp = o.remove(target);
                    boolean got = da.removeFirst(target);
                    check(exp == got, "removeFirst result mismatch");
                } else if (op < 96) { // clear
                    log.accept("clear()");
                    da.clear(); o.clear();
                } else if (op < 98) { // get (spot check)
                    if (!o.isEmpty()) {
                        int idx = r.nextInt(o.size());
                        log.accept("get(" + idx + ")");
                        Integer exp = o.get(idx);
                        Integer got = da.get(idx);
                        check(java.util.Objects.equals(exp, got), "get mismatch");
                    } else {
                        log.accept("get(0) // expect throw");
                        boolean threw = false;
                        try { da.get(0); } catch (IndexOutOfBoundsException e) { threw = true; }
                        check(threw, "get on empty should throw");
                    }
                } else { // sort occasionally
                    boolean hasNull = o.contains(null);
                    log.accept("sort()");
                    boolean thrown = false;
                    try {
                        da.sort();
                    } catch (NullPointerException e) {
                        thrown = true;
                    }
                    if (hasNull) {
                        check(thrown, "DA.sort should throw on nulls");
                    } else {
                        o.sort(java.util.Comparator.naturalOrder());
                        assertSameContent(o, da);
                    }
                }
            } catch (AssertionError | RuntimeException ex) {
                dumpLog.run();
                throw new RuntimeException(
                        "Fuzz failure at step " + step + " seed=" + seed + ": " + ex.getMessage(), ex);
            }

            // periodic full content check to catch drift early
            if ((step & 255) == 0) {
                assertSameContent(o, da);
            }
        }

        // final check
        assertSameContent(o, da);
    }



    /* ----------------------------- Main ----------------------------- */

    public static void main(String[] args) {
        System.out.println("Testing DynamicArray…");
        testBasics();
        testAppendPrependGrowth();
        testAddMiddleAndBounds();
        testSetGetRemoveVariants();
        testRemoveFirstWithNulls();
        testClearAndReuse();
        testSortBasicAndStability();

        long seed = System.currentTimeMillis();
        int ops = 200_000; // increase for more stress if needed
        System.out.println("Starting fuzz with seed=" + seed + " ops=" + ops);
        randomizedFuzz(seed, ops);

        // fixed-seed short fuzz for CI stability
        // randomizedFuzz(123456789L, 200_000);

        System.out.println("Success! All tests passed.");
    }
}
