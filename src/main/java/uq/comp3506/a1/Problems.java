// @edu:student-assignment

package uq.comp3506.a1;

// This is part of COMP3506 Assignment 1. Students must implement their own solutions.

import uq.comp3506.a1.structures.DynamicArray;

/**
 * Supplied by the COMP3506/7505 teaching team, Semester 2, 2025.
 */
public class Problems {

    /**
     * Return a string representing the RLE of input
     * <p>
     * Bounds:
     * - Basic tests
     * input will have up to 10 characters
     * - Advanced tests
     * input will have up to 1000 characters
     * - Welcome to COMP3506
     * input will have up to 100'000 characters
     */
    public static String shortRuns(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        int run = 1;
        for (int i = 1; i < input.length(); i++) {
            if (input.charAt(i) == input.charAt(i - 1)) {
                run++;
            } else {
                char c = input.charAt(i - 1);
                while (run > 9) {
                    out.append(c).append(9);
                    run -= 9;
                }
                out.append(c).append(run);
                run = 1;
            }
        }
        // appends the last run of characters
        char c = input.charAt(input.length() - 1);
        while (run > 9) {
            out.append(c).append(9);
            run -= 9;
        }
        out.append(c).append(run);
        return out.toString();
    }

    /**
     * helper for finding largest element in array
     */
    public static long findMax(Long[] arr) {
        long max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
            }
        }
        return max;
    }

    /**
     * Return the maximum score that can be achieved within exactly
     * "turns" turns
     * - values in array are guaranteed to be >= 0
     * <p>
     * Bounds:
     * - Basic tests
     * array will consist of up to 100 elements
     * Each element will be up to 100
     * There will be up to 10 turns
     * - Advanced tests
     * array will consist of up to 10'000 elements
     * Each element will be up to 10'000
     * There will be up to 10'000 turns
     * - Welcome to COMP3506
     * array will consist of up to 100'000 elements
     * Each element will be up to 10'000'000
     * There will be up to 10'000'000 turns
     */
    public static long arithmeticRules(Long[] array, long turns) {
        if (turns <= 0 || array.length == 0) {
            return 0;
        }
        long max = findMax(array);
        return (max * turns + (turns - 1) * turns / 2);
    }

    /**
     * Return the epsilon-approximate square root of "number"
     * - epsilon will be in [0.00001, 1]
     * <p>
     * Bounds:
     * - Basic tests
     * number will be up to 1000
     * - Advanced tests
     * number will be p to 1'000'000
     * - Welcome to COMP3506
     * number will be up to 10**16 (ten to the power 16)
     */
    public static double sqrtHappens(long number, double epsilon) {
        if (number == 0) {
            return 0.0;
        }
        double difference = 2;
        double answer = (double) number / 2;
        while (difference > epsilon) {
            double newIter = 0.5 * (answer + (number / answer));
            difference = Math.abs(newIter * newIter - number);
            answer = newIter;
        }
        return answer;
    }

    /**
     * Return the largest integer in numbers repeated an odd number of times
     * - values in "numbers" will be in the range [0, 2^32 - 1]
     * <p>
     * Bounds:
     * - Basic tests
     * There will be up to 100 numbers in the array
     * - Advanced tests
     * There will be up to 10'000 numbers in the array
     * - Welcome to COMP3506
     * There will be up to 100'000 numbers in the array
     */
    public static long spaceOddity(Long[] numbers) {
        if (numbers.length == 0) {
            return -1;
        }
        DynamicArray<Long> arr = new DynamicArray<>();
        for (Long number : numbers) {
            arr.append(number);
        }
        arr.sort();
        int i = arr.size() - 1;
        while (i >= 0) {
            long v = arr.get(i);
            int count = 1;
            i--;
            while (i >= 0 && arr.get(i).equals(v)) {
                count++;
                i--;
            }

            if ((count & 1) == 1) {
                return v;
            }
        }
        return -1;
    }

    /**
     * Helper method to convert any number to base k, stored in a given buffer.
     * returns the length of the base k number
     *
     * @param x number
     * @param k base
     * @param buf given buffer
     * @return length of base k number
     */
    private static int toBaseK(long x, long k, int[] buf) {
        if (x == 0) {
            buf[0] = 0;
            return 1;
        }

        int len = 0;

        int[] tmp = new int[70]; //at most 64 bits
        while (x > 0) {
            tmp[len++] = (int) (x % k); //LSB goes in first
            x /= k; //integer division gets rids of LSB
        }
        // reverse to MSB→LSB
        for (int i = 0; i < len; i++) {
            buf[i] = tmp[len - 1 - i];
        }
        return len;
    }

    /**
     * gives back how many numbers SMALLER or EQUAL to X base k is k-freaky
     */
    private static long countUpTo(long x, long k) {
        // Negative bound is just empty set.
        if (x < 0) {
            return 0;
        }
        // base 1
        // can only be list of zeros, only 0 or 1 can be 1-freaky
        if (k == 1)  {
            return Math.min(x, 1) + 1; // 1 if {0}, 2 if {0,1}
        }
        // every number is 2-freaky
        // all integers up to X inclusive
        if (k == 2) {
            return x + 1;
        }

        // Convert X to base-k digits
        int[] digits = new int[70];
        int digitCount = toBaseK(x, k, digits); // amount of digits in base k

        long count = 0L;
        // walk MSB->LSB.
        // for each digit, add the number of possible ways the remaining digits
        // can be(0 or 1) to count
        // if a digit is 0 then you cannot change this digit to make the number smaller,
        // so it doesnt add to the possibilities
        // if a digit is 1 then you can change it to a 0 to count the number small than X,
        // giving 2^remaining digits possibilities
        // if a digit is 2 or above then changing it to 0 or 1 will make it smaller,
        // making 2* 2^ remaining digits possibilities
        for (int i = 0; i < digitCount; i++) {
            int digit = digits[i];
            int rem = digitCount - 1 - i; // how many positions remain after i

            if (digit == 0) {
                continue;
            }

            if (digit == 1) {
                count += 1L << rem; // 2^rem patterns for the digits after it
                continue;
            }
            count += 1L << (rem + 1); //2^2rem patterns for the digits after it

            // if LSB of X is 0 or 1 we would continue and exit the loop,
            // so if we reach here X is not k-freaky

            return count;
        }

        // if we finished the loop, LSB digit of X was 0 or 1.
        // X  is k-freaky and should be included.
        return count + 1;
    }



    /**
     * Return the number of k-freaky numbers in the range [m, n]
     * <p>
     * Bounds:
     * - Basic tests
     * m and n will be up to 1000
     * k will be up to 10
     * - Advanced tests
     * m and n will be up to 1'000'000
     * k will be up to 100
     * - Welcome to COMP3506
     * m and n will be up to 10**15 (ten to the power 15)
     * k will be up to 10'000
     */
    public static long freakyNumbers(long m, long n, long k) {
        long low = Math.min(m, n);
        long high = Math.max(m, n);
        // countUpTo(m) - countUpTO(n) = count([m,n])
        return countUpTo(high, k) - countUpTo(low - 1, k);
    }

    private static final long IMPOSSIBLE = 1L << 60;
    // stores all the permutations of cut on long and short side, and its associated cost
    private static long[][][] listMemo;

    /**
     * top down recursive function for solving the minimum cost
     * @param m side 1
     * @param n side 2
     * @param k desired squares
     * @return best cost
     */
    private static long findBest(int m, int n, int k) {

        int shortSide = m;
        int longSide = n;
        if (m > n) {
            shortSide = n;
            longSide = m;
        }

        final int area = m * n;
        if (k < 0 || k > area) {
            return IMPOSSIBLE;
        }
        if (k == 0 || k == area) {
            return 0L;
        }

        k = Math.min(k, area - k); //optimization to find the smaller piece

        long cached;
        // Memo lookup (memo is sized so that indices are in-range)
        if (shortSide <= listMemo.length - 1 && longSide
                <= listMemo[0].length - 1 && k <= listMemo[0][0].length - 1) {
            cached = listMemo[shortSide][longSide][k];
        } else {
            cached = -2L; // -2 means not in array for state
        }
        if (cached >= 0) {
            return cached;
        }

        if (m == 1) { // if its a strip then anything shorter is achievable in one cut
            return 1L;
        }
        if (n == 1) {
            return 1L;
        }

        long best = IMPOSSIBLE;

        if (k % shortSide == 0) { // if k is a multiple of m or n then one cut is possible
            best = Math.min(best, (long) shortSide * shortSide);
        }
        if (k % longSide == 0) {
            best = Math.min(best, (long) longSide * longSide);
        }

        // try breaking on the short side
        for (int i = 1; i <= shortSide - 1; i++) {
            long base = (long) longSide * longSide; // base cost for splitting along the short side

            // rules out the further split values that is infeasible
            int lowestBreak = Math.max(0, k - (shortSide - i) * longSide);
            int highestBreak = Math.min(k, i * longSide);
            for (int j = lowestBreak; j <= highestBreak; j++) {
                long div1 = findBest(i, longSide, j); //recursively call for sub-rectangles
                if (div1 >= IMPOSSIBLE) {
                    continue;
                }
                long div2 = findBest(shortSide - i, longSide, k - j);
                if (div2 >= IMPOSSIBLE) {
                    continue;
                }
                long total = base + div1 + div2;
                if (total < best) {
                    // if value of these breaks added from recursion are
                    // better than the best one so far, update best
                    best = total;
                }
            }
        }

        // try breaking on the long side
        for (int x = 1; x <= longSide - 1; x++) {
            long base = (long) shortSide * shortSide;


            int lowestBreak = Math.max(0, k - shortSide * (longSide - x));
            int highestBreak = Math.min(k, shortSide * x);

            for (int s = lowestBreak; s <= highestBreak; s++) {
                long c1 = findBest(shortSide, x, s);
                if (c1 >= IMPOSSIBLE) {
                    continue;
                }
                long c2 = findBest(shortSide, longSide - x, k - s);
                if (c2 >= IMPOSSIBLE) {
                    continue;
                }
                long total = base + c1 + c2;
                if (total < best) {
                    best = total;
                }
            }
        }
        listMemo[shortSide][longSide][k] = best; // stores the best one so far
        return best;
    }

    /**
     * Return the optimal (minimum) cost of breaking the chocolate
     * <p>
     * Bounds:
     * - Basic tests
     * m and n will be up to 5
     * k will be up to 25
     * - Advanced tests
     * m and n will be up to 5
     * k will be up to 25
     * (bounds are the same but test cases are more difficult)
     * - Welcome to COMP3506
     * m and n will be up to 10
     * k will be up to 100
     */
    public static long lifeIsSweet(int m, int n, int k) {
        if (m < 0 || n < 0 || k < 0) {
            return IMPOSSIBLE;
        }
        if (m == 0 || n == 0) {
            if (k == 0) {
                return 0;
            } else {
                return IMPOSSIBLE;
            }
        }

        // determines what the maximum size memo we need for each input
        int shortSideMax = Math.min(m, n);
        int longSideMax = Math.max(m, n);
        int maxK = Math.max(0, Math.min(k, m * n));
        listMemo = new long[shortSideMax + 1][longSideMax + 1][maxK + 1];
        for (int a = 0; a <= shortSideMax; a++) {
            for (int b = 0; b <= longSideMax; b++) {
                for (int t = 0; t <= maxK; t++) {
                    listMemo[a][b][t] = -1L; // sets all to -1 for unknown/unexplored
                }
            }
        }

        return findBest(m, n, k);
    }
}
