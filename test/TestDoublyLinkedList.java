import java.util.*;
import uq.comp3506.a1.structures.DoublyLinkedList;

public class TestDoublyLinkedList {

    /* ---------- Helpers ---------- */

    private static <T> void assertListEquals(List<T> oracle, DoublyLinkedList<T> dll) {
        assert dll.size() == oracle.size() : "size mismatch: dll=" + dll.size() + " oracle=" + oracle.size();

        // Check full forward content equality via get(i)
        for (int i = 0; i < oracle.size(); i++) {
            T a = oracle.get(i);
            T b = dll.get(i);
            if (!Objects.equals(a, b)) {
                throw new AssertionError("content mismatch at index " + i + ": expected=" + a + " actual=" + b);
            }
        }

        // Check head/tail convenience accessors
        Object expFirst = oracle.isEmpty() ? null : oracle.get(0);
        Object expLast  = oracle.isEmpty() ? null : oracle.get(oracle.size() - 1);
        Object gotFirst = dll.getFirst();
        Object gotLast  = dll.getLast();
        assert Objects.equals(expFirst, gotFirst) : "getFirst mismatch: exp=" + expFirst + " got=" + gotFirst;
        assert Objects.equals(expLast, gotLast)   : "getLast mismatch: exp=" + expLast + " got=" + gotLast;
        assert (dll.isEmpty() == oracle.isEmpty()) : "isEmpty mismatch";
    }

    /** Random value generator with nulls + duplicates to stress removeFirst equality. */
    private static Integer randValue(Random r) {
        // ~10% nulls, otherwise many duplicates in [-5, 5]
        int roll = r.nextInt(10);
        if (roll == 0) return null;
        return r.nextInt(11) - 5;
    }

    /* ---------- Deterministic small tests ---------- */

    public static void testCreateAndSize() {
        DoublyLinkedList<String> dll = new DoublyLinkedList<>();
        assert dll.size() == 0;
        assert dll.isEmpty();
        assert dll.getFirst() == null;
        assert dll.getLast() == null;
    }

    public static void testInsertionBasics() {
        DoublyLinkedList<Integer> dll = new DoublyLinkedList<>();
        dll.append(10);
        assert dll.size() == 1;
        Integer old = dll.set(0, 123);
        assert Objects.equals(old, 10);
        assert Objects.equals(dll.get(0), 123);
        dll.prepend(11);
        assert dll.size() == 2;
        assert Objects.equals(dll.get(0), 11);
        assert Objects.equals(dll.get(1), 123);

        // add at bounds
        dll.add(0, 7);                 // insert at head
        dll.add(dll.size(), 99);       // append via add(size, x)
        assert dll.size() == 4;
        assert Objects.equals(dll.get(0), 7);
        assert Objects.equals(dll.get(3), 99);
    }

    public static void testRemoveCases() {
        DoublyLinkedList<String> dll = new DoublyLinkedList<>();

        // remove from single-element list
        dll.append("x");
        String removed = dll.remove(0);
        assert Objects.equals(removed, "x");
        assert dll.size() == 0 && dll.isEmpty();
        assert dll.getFirst() == null && dll.getLast() == null;

        // remove head, middle, tail
        dll.append("a");
        dll.append("b");
        dll.append("c");
        dll.append("d");
        assert Objects.equals(dll.remove(0), "a");   // remove head
        assert Objects.equals(dll.remove(1), "c");   // remove middle (now list is [b,d])
        assert Objects.equals(dll.remove(1), "d");   // remove tail
        assert Objects.equals(dll.remove(0), "b");   // remove last remaining
        assert dll.isEmpty();
    }

    public static void testRemoveFirstEqualityAndNulls() {
        DoublyLinkedList<Integer> dll = new DoublyLinkedList<>();
        // [null, 1, null, 1, 2]
        dll.append(null);
        dll.append(1);
        dll.append(null);
        dll.append(1);
        dll.append(2);

        // removeFirst(null) should remove the first null only
        assert dll.removeFirst(null);
        assert dll.size() == 4;
        assert Objects.equals(dll.get(0), 1);
        assert Objects.equals(dll.get(1), null); // second null still present

        // removeFirst(42) not found
        assert !dll.removeFirst(42);

        // removeFirst(1) removes only the first "1"
        assert dll.removeFirst(1);
        assert dll.size() == 3;
        assert Objects.equals(dll.get(0), null) || Objects.equals(dll.get(0), 1) || Objects.equals(dll.get(0), 2);
        // We won’t rely on order here further—random test will.
    }

    /** Custom type to ensure removeFirst uses equals(), not reference equality. */
    static final class Box {
        final int v;
        Box(int v) { this.v = v; }
        @Override public boolean equals(Object o) { return (o instanceof Box b) && b.v == v; }
        @Override public int hashCode() { return Integer.hashCode(v); }
        @Override public String toString() { return "Box(" + v + ")"; }
    }

    public static void testCustomEquals() {
        DoublyLinkedList<Box> dll = new DoublyLinkedList<>();
        Box b1a = new Box(1);
        Box b1b = new Box(1);
        Box b2  = new Box(2);
        dll.append(b1a);
        dll.append(b2);
        // removeFirst must match by equals, so removing "new Box(1)" should remove b1a
        boolean ok = dll.removeFirst(new Box(1));
        assert ok : "removeFirst(Box(1)) should succeed";
        assert dll.size() == 1;
        assert Objects.equals(dll.get(0), b2);
    }

    public static void testExceptions() {
        DoublyLinkedList<Integer> dll = new DoublyLinkedList<>();
        // get/set/remove on empty or OOB
        try { dll.get(0); assert false : "get(0) should throw on empty"; } catch (IndexOutOfBoundsException expected) {}
        try { dll.set(0, 1); assert false : "set(0) should throw on empty"; } catch (IndexOutOfBoundsException expected) {}
        try { dll.remove(0); assert false : "remove(0) should throw on empty"; } catch (IndexOutOfBoundsException expected) {}
        try { dll.get(-1); assert false : "get(-1) should throw"; } catch (IndexOutOfBoundsException expected) {}

        dll.append(5);
        try { dll.get(1); assert false : "get(size) must throw"; } catch (IndexOutOfBoundsException expected) {}
        try { dll.set(1, 2); assert false : "set(size) must throw"; } catch (IndexOutOfBoundsException expected) {}
        try { dll.remove(1); assert false : "remove(size) must throw"; } catch (IndexOutOfBoundsException expected) {}
        try { dll.add(2, 9); assert false : "add(size+1) must throw"; } catch (IndexOutOfBoundsException expected) {}

        // add at size is allowed
        dll.add(dll.size(), 9);
        assert dll.size() == 2;
    }

    /* ---------- Large-scale randomized testing (oracle = java.util.LinkedList) ---------- */

    public static void randomizedFuzz(long seed, int operations) {
        Random r = new Random(seed);
        DoublyLinkedList<Integer> dll = new DoublyLinkedList<>();
        LinkedList<Integer> oracle   = new LinkedList<>();

        for (int step = 0; step < operations; step++) {
            int op = r.nextInt(100);
            try {
                if (op < 22) { // 22% append
                    Integer v = randValue(r);
                    boolean ok = dll.append(v);
                    oracle.addLast(v);
                    assert ok;
                } else if (op < 40) { // 18% prepend
                    Integer v = randValue(r);
                    boolean ok = dll.prepend(v);
                    oracle.addFirst(v);
                    assert ok;
                } else if (op < 58) { // 18% add at random index (including add(size))
                    Integer v = randValue(r);
                    int idx = (oracle.isEmpty()) ? 0 : r.nextInt(oracle.size() + 1);
                    boolean ok = dll.add(idx, v);
                    oracle.add(idx, v);
                    assert ok;
                } else if (op < 70) { // 12% set at random index
                    if (!oracle.isEmpty()) {
                        int idx = r.nextInt(oracle.size());
                        Integer v = randValue(r);
                        Integer exp = oracle.set(idx, v);
                        Integer got = dll.set(idx, v);
                        assert Objects.equals(exp, got) : "set return mismatch";
                    }
                } else if (op < 85) { // 15% remove at random index
                    if (!oracle.isEmpty()) {
                        int idx = r.nextInt(oracle.size());
                        Integer exp = oracle.remove(idx);
                        Integer got = dll.remove(idx);
                        assert Objects.equals(exp, got) : "remove return mismatch";
                    } else {
                        // Try to provoke exceptions on empty remove sometimes
                        if (r.nextBoolean()) {
                            try { dll.remove(0); assert false : "remove on empty should throw"; } catch (IndexOutOfBoundsException expected) {}
                        }
                    }
                } else if (op < 93) { // 8% removeFirst(value)
                    Integer target = randValue(r);
                    boolean exp = oracle.removeFirstOccurrence(target);
                    boolean got = dll.removeFirst(target);
                    assert exp == got : "removeFirstOccurrence mismatch";
                } else if (op < 96) { // 3% clear
                    dll.clear();
                    oracle.clear();
                } else if (op < 98) { // 2% get at random index
                    if (!oracle.isEmpty()) {
                        int idx = r.nextInt(oracle.size());
                        Integer exp = oracle.get(idx);
                        Integer got = dll.get(idx);
                        assert Objects.equals(exp, got) : "get mismatch";
                    } else {
                        // provoke failure
                        try { dll.get(0); assert false : "get on empty should throw"; } catch (IndexOutOfBoundsException expected) {}
                    }
                } else { // 2% bounds-negative and size+1 probes
                    try { dll.get(-1); assert false : "get(-1) should throw"; } catch (IndexOutOfBoundsException expected) {}
                    try { dll.set(-1, 0); assert false : "set(-1) should throw"; } catch (IndexOutOfBoundsException expected) {}
                    int sz = oracle.size();
                    try { dll.get(sz); assert false : "get(size) should throw"; } catch (IndexOutOfBoundsException expected) {}
                    try { dll.set(sz, 0); assert false : "set(size) should throw"; } catch (IndexOutOfBoundsException expected) {}
                    try { dll.remove(sz); assert false : "remove(size) should throw"; } catch (IndexOutOfBoundsException expected) {}
                    try { dll.add(sz + 1, 0); assert false : "add(size+1) should throw"; } catch (IndexOutOfBoundsException expected) {}
                }
            } catch (AssertionError ae) {
                // Include seed + step for reproducible debugging
                throw new AssertionError("Fuzz assertion failed at step " + step + " (seed=" + seed + "): " + ae.getMessage(), ae);
            } catch (RuntimeException re) {
                throw new RuntimeException("Unexpected exception at step " + step + " (seed=" + seed + ")", re);
            }

            // Periodically verify full content + invariants
            if (step % 100 == 0) {
                assertListEquals(oracle, dll);
            }
        }
        // Final comprehensive check
        assertListEquals(oracle, dll);
    }

    /* ---------- “Hints” from your skeleton ---------- */
    public static void hints() {
        System.out.println("Run with -ea to enable assertions.");
        System.out.println("Random fuzzing cross-checks against java.util.LinkedList.");
        System.out.println("Seed is printed so you can reproduce failures.");
    }

    /* ---------- Main ---------- */
    public static void main(String[] args) {
        System.out.println("Testing DoublyLinkedList...");
        hints();

        // Deterministic unit tests
        testCreateAndSize();
        testInsertionBasics();
        testRemoveCases();
        testRemoveFirstEqualityAndNulls();
        testCustomEquals();
        testExceptions();

        // Large randomized fuzzing — adjust operations for speed if needed.
        long seed = System.currentTimeMillis();  // Replace with fixed seed to reproduce a failure.
        int ops   = 50_000;                      // Increase to 500k+ for stress.
        System.out.println("Starting fuzz with seed=" + seed + ", ops=" + ops);
        randomizedFuzz(seed, ops);

        // Also run a fixed-seed quick pass for CI stability
        randomizedFuzz(123456789L, 10_000);

        System.out.println("Success! All tests passed.");
    }
}