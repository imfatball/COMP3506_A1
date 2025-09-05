// @edu:student-assignment

package uq.comp3506.a1;

/**
 * Supplied by the COMP3506/7505 teaching team, Semester 2, 2025.
 */
public class DNAStructure {
    private final int width;
    private final char[] window;
    private int size;
    private int head;
    private int[] count;


    /**
     * Construct at empty DNAStructure object that can store w chars.
     */
    public DNAStructure(int width) {
        if (width < 0) {
            throw new IllegalArgumentException("w must be >= 0");
        }
        this.width = width;
        this.window = new char[width];
        this.size = 0;
        this.head = 0;
        this.count = new int[4]; // counts of A,C,G,T, updated on slide for O(1) fetch
    }

    /**
     * Return true if the structure is full, false otherwise
     */
    public boolean isFull() {
        return size == width;
    }

    /**
     * Remove the oldest character, and add the newest one
     * Full marks: O(1) or O(1*) worst-case
     */
    public void slide(char c) {
        if (!isFull()) {
            // Place new char at the "tail" position
            window[(head + size) % width] = c;
            size++;
            count[encode(c)]++;
        } else {
            char old = window[head];
            count[encode(old)]--;
            window[head] = c; //replace head with new char, move the head forward one(circular)
            count[encode(c)]++;

            head = (head + 1) % width;
        }
    }

    /**
     * Return the number of times c appears in the current window
     * Full marks: O(1) worst-case
     */
    public int count(char c) {
        return count[encode(c)];
    }


    /**
     * Return the number of unique sequences of length k currently stored
     * in the window that repeat at least once in the window.
     * For example, consider the window contains: gtcgtcgtc and k=4
     * We would return 3 because:
     * 'gtcg' and 'tcgt' and 'cgtc' all repeat in the window.
     * Full marks: O(w) worst-case
     * Partial marks: O(wk) worst-case
     * Note: k will be in the range [2, 13], and 2 <= k <= w
     */
    public int countRepeats(int k) {

        if (k < 2 || k > size) {
            return 0;
        }

        final int n = size;
        final int numKmers = n - k + 1; // total number of k-mers
        // find the lowest power of 2 that is bigger than number of k-mers
        final int capPow2 = highestPowerOfTwoAtLeast(Math.max(8, numKmers * 2));
        final IntToIntHash freq = new IntToIntHash(capPow2);

        int code = 0;
        for (int i = 0; i < k; i++) {
            // window index of the i-th char inside the current logical window
            int idx = (head + i) % width;
            // accumulate 2 bits for this base
            code = (code << 2) | encode(window[idx]);
        }
        // Record frequency of the first k-mer
        freq.put(code, freq.get(code) + 1);


        final int mask = (1 << (2 * k)) - 1; // mask for 2k bits k = 3: (0b111111) for rolling

        //roll over the rest of the window
        for (int i = 1; i <= n - k; i++) { //already did 0, start from 1 to n - k(number of k-mers)

            int inIdx = (head + i + k - 1) % width;
            int inChar   = encode(window[inIdx]);

            //shift left by 2
            //remove top 2 bits(& 00111111...)
            //or in new data(1111...1100 OR inChar)
            code = ((code << 2) & mask) | inChar;

            // Tally into hashtable
            freq.put(code, freq.get(code) + 1);
        }


        int repeats = 0;
        for (int i = 0; i < freq.cap; i++) {
            if (freq.used[i] && freq.vals[i] >= 2) {
                repeats++;
            }
        }
        return repeats;
    }


    /**
     * helper for countRepeats
     * takes in int x and returns smallest 2^n that is bigger than x (min 8)
     */
    private static int highestPowerOfTwoAtLeast(int x) {
        int c = 8;
        while (c < x) {
            c <<= 1;
        }
        return c;
    }

    /**
     * hash table that stores int key:int value
     * used for storing encode(k-mers):frequency for fast look up
     */
    private static final class IntToIntHash {
        final int mask;
        final int cap;
        final int[] keys;
        final int[] vals;
        final boolean[] used;

        IntToIntHash(int capacityPow2) {
            this.cap = capacityPow2;
            this.mask = cap - 1; // 01111111......1111 len of log2(cap)
            this.keys = new int[cap];
            this.vals = new int[cap];
            this.used = new boolean[cap];
        }

        /**
         * look up value for key k, return 0 if not found
         */
        int get(int k) {
            int i = k & mask;
            while (used[i]) { // linear search through empty slot or key
                if (keys[i] == k) {
                    return vals[i];
                }
                i = (i + 1) & mask;
            }
            return 0;
        }

        void put(int k, int v) {
            int i = k & mask;
            while (true) {
                if (!used[i]) { // claim empty slot
                    used[i] = true;
                    keys[i] = k;
                    vals[i] = v;
                    return;
                } else if (keys[i] == k) { //update
                    vals[i] = v;
                    return;
                } else {
                    i = (i + 1) & mask; // linear search
                }
            }
        }
    }

    /**
     * Return true if the window contains a palindrome of length k. Remember
     * that DNA palindromes are different to typical English word palindromes.
     * Full marks: O(w) best-case, and O(wk) worst-case.
     * Again, k will be in the range [2, 13], and 2 <= k <= w
     */
    public boolean hasPalindrome(int k) {
        if (k < 2 || k > size) {
            return false;
        }
        final int n = size;

        // Build the first forward code of k bases
        int forward = 0;
        for (int i = 0; i < k; i++) {
            int idx = (head + i) % width; //logical index
            // encode first k bases into binary for comparison
            forward = (forward << 2) | encode(window[idx]);
        }

        // Build the first reverse complement encoding of the same position
        int reverseComplement = 0;
        final int topShift = 2 * (k - 1); // bit position of the topmost 2-bit lane
        for (int i = 0; i < k; i++) {
            int v = encode(window[(head + i) % width]);
            // push in complements from the right
            reverseComplement = (reverseComplement >>> 2) | (complementBase2(v) << topShift);
        }

        // If first k-mer is a palindrome, we can return early
        if (forward == reverseComplement) {
            return true;
        }


        final int mask = (1 << (2 * k)) - 1; // same mask in countRepeat


        for (int i = 1; i <= n - k; i++) { // same procedure to roll over the window as countRepeat
            int inIdx = (head + i + k - 1) % width;
            int inChar = encode(window[inIdx]);

            forward = ((forward << 2) & mask) | inChar;
            reverseComplement = (reverseComplement >>> 2) | (complementBase2(inChar) << topShift);

            if (forward == reverseComplement) {
                return true;
            }
        }

        return false;
    }

    /**
     * returns the binary complement of the given encoded base
     */
    private static int complementBase2(int v) {
        return v ^ 0b11;
    }

    /**
     * helper for encoding base into int
     */
    private int encode(char c) {
        if (c == 'A') {
            return 0;
        } else if (c == 'C') {
            return 1;
        } else if (c == 'G') {
            return 2;
        } else if (c == 'T') {
            return 3;
        } else {
            return -1;
        }
    }

    /**
     * Return a string representation of the data structure. It should just be
     * a single string containing the oldest to newest character in that order.
     * If the window is not full, just return the populated characters.
     */
    public String stringify() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(window[(head + i) % width]);
        }
        return sb.toString();
    }
}
