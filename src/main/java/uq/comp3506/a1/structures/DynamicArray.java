// @edu:student-assignment

package uq.comp3506.a1.structures;

/**
 * Supplied by the COMP3506/7505 teaching team, Semester 2, 2025.
 *
 * NOTE: You should go and carefully read the documentation provided in the
 * ListInterface.java file - this explains some of the required functionality.
 */
public class DynamicArray<T extends Comparable<T>> implements ListInterface<T> {

    /**
     * size tracks the total number of slots being used in the data array
     */
    private int size = 0;

    /**
     * capacity tracks the total number of slots (used or unused) in the data array
     */
    private int capacity = 0;

    /**
     * data stores the raw objects
     */
    private T[] data;


    /**
     *  tracks the start of the array to allow circular storage
     */
    private int start = 0;

    /**
     * Constructs an empty Dynamic Array
     */
    public DynamicArray() {
        capacity = 1;
        data = (T[]) new Comparable[capacity];
    }

    // See ListInterface
    @Override
    public int size() {
        return size;
    }

    // See ListInterface
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Has the size reached the current capacity?
     * Return true if so, false otherwise.
     * This is merely a convenience function for you. We will not be
     * testing it explicitly.
     */
    public boolean isFull() {
        return size == capacity;
    }

    /**
     * Get current capacity.
     * Again, this is merely a convenience function for you. We will not
     * be testing it explicitly.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     *  helper that doubles the capacity
     */
    private void double_capacity() {
        int newCap = capacity * 2;
        T[] newData = (T[]) new Comparable[newCap];

        for (int i = 0; i < size; i++) {
            newData[i] = data[(start + i) % capacity]; // copies and unravels the data
        }
        data = newData;
        capacity = newCap;
        start = 0;
    }

    private int at(int i) {
        return (start + i) % capacity;
    }

    /**
     * Checks the bounds on the DA
     * Note that if exclusive is true, [0, size-1] are valid bounds;
     * if exclusive is false, the check will use [0, size] as valid bounds.
     */

    private void checkBounds(int idx, boolean exclusive) {
        int upperBound = (exclusive) ? size : size + 1;
        if (idx < 0 || idx >= upperBound) {
            throw new IndexOutOfBoundsException("Index " + idx + " is out of bounds.");
        }
    }

    /**
     * Add an element to the end of the array. Returns true if successful,
     * false otherwise.
     * Time complexity for full marks: O(1*)
     * That is, O(1) *amortized*.
     */
    @Override
    public boolean append(T element) {
        if (isFull()) {
            double_capacity();
        }
        int newIndex = at(size);
        data[newIndex] = element;
        size++;
        return true;
    }

    /**
     * Add an element to the beginning of the array. Returns true if successful,
     * false otherwise.
     * Time complexity for full marks: O(1*)
     * Again, O(1*) means constant amortized time.
     */
    @Override
    public boolean prepend(T element) {
        if (isFull()) {
            double_capacity();
        }
        start = (start - 1 + capacity) % capacity;
        data[start] = element;
        size++;
        return true;
    }

    /**
     * Add element to index ix.
     * Note: This does not overwrite the element at index ix - that is what
     * the set() method is for, see below. Instead, this function is similar
     * to append or prepend, but it adds the element at a desired index.
     * If ix is out of bounds, throw an IndexOutOfBoundsException.
     * Acceptable bounds are [0, size] where 0 will be prepend, size will
     * be append, and anything in between will need to shuffle elements around.
     * Time complexity for full marks: O(N)
     */
    @Override
    public boolean add(int ix, T element) {
        checkBounds(ix, false);
        if (ix == 0) {
            return prepend(element);
        }
        if (ix == size) {
            return append(element);
        }

        if (isFull()) {
            double_capacity();
        }

        for (int i = size - 1; i >= ix; i--) { // shuffles all after ix one to the right
            data[at(i + 1)] = data[at(i)];
        }

        data[at(ix)] = element;
        size++;
        return true;
    }


    /**
     * Return the element at index ix.
     * If ix is out of bounds, throw an IndexOutOfBoundsException.
     * Time complexity for full marks: O(1)
     */
    @Override
    public T get(int ix) {
        checkBounds(ix, true);
        return data[at(ix)];
    }

    /**
     * Overwrite the "old" value at ix with element, and return the old value.
     * If ix is out of bounds, throw an IndexOutOfBoundsException.
     * Time complexity for full marks: O(1)
     */
    @Override
    public T set(int ix, T element) {
        checkBounds(ix, true);
        T oldData = data[at(ix)];
        data[at(ix)] = element;
        return oldData;
    }

    /**
     * Remove and return the value at index ix
     * If ix is out of bounds, throw an IndexOutOfBoundsException.
     * Time complexity for full marks: O(N)
     */
    @Override

    public T remove(int ix) {
        checkBounds(ix, true);
        final T old = data[at(ix)];
        for (int j = ix; j < size - 1; j++) { // shifts everything past ix one to the left
            data[at(j)] = data[at(j + 1)];
        }
        data[at(size - 1)] = null; // turn the now empty tail slot into null
        size--;
        return old;
    }



    /**
     * Find and remove the first value in the array that equals t (the one
     * with the smallest index).
     * Return true if successful, false otherwise.
     * Time complexity for full marks: O(N)
     */
    @Override
    public boolean removeFirst(T t) {
        for (int i = 0; i < size; i++) {
            if ((t == null ? data[at(i)] == null : t.equals(data[at(i)]))) {
                remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        data = (T[]) new Comparable[capacity];
        start = 0;
        size = 0;
    }

    /**
     * Sort all of the elements inside the array.
     *
     * Time complexity for full marks: O(NlogN).
     * That is, we expect you to implement a sorting algorithm that runs in
     * "n log n" time. This may be in expectation, or guaranteed worst case.
     *
     * A note on comparisons:
     *
     * You may assume that any type stored inside the DynamicArray already
     * implements Comparable which means you can just use compareTo()
     * in order to sort elements.
     *
     * We will assume sorting in ascending, so you will want to do something
     * like: if (data[i].compareTo(data[j]) < 0) { // data[i] < data[j] }
     */
    public void sort() {
        int n = size;
        if (n == 0) {
            return;
        }

        T[] temp = (T[]) new Comparable[n];
        for (int i = 0; i < n; i++) {
            temp[i] = data[at(i)];
        }

        // No nulls allowed in array
        for (int i = 0; i < n; i++) {
            if (temp[i] == null) {
                throw new NullPointerException("Cannot sort null elements");
            }
        }

        if (n == 1) {
            data[0] = temp[0];
            start = 0;
            return;
        }

        T[] dest = (T[]) new Comparable[n];

        // merge sort, bottom up, starts at width 1, merge sort, combine,
        // and double width until the width is larger than the size
        for (int width = 1; width < n; width <<= 1) {
            int left = 0;
            while (left < n) {
                int mid = Math.min(left + width, n);
                int right = Math.min(left + (width << 1), n);
                int i = left;
                int j = mid;
                int k = left;
                // element by element compare and slot in for the combine step of merge sort
                while (i < mid && j < right) {
                    if (temp[i].compareTo(temp[j]) <= 0) {
                        dest[k++] = temp[i++];
                    } else {
                        dest[k++] = temp[j++];
                    }
                }

                while (i < mid) {
                    dest[k++] = temp[i++];
                }
                while (j < right) {
                    dest[k++] = temp[j++];
                }
                left = right;
            }
            T[] tmp = temp;
            temp = dest;
            dest = tmp; // swap temp array to bounce between arrays while swapping
        }

        for (int i = 0; i < n; i++) { // Copy sorted back and normalize
            data[i] = temp[i];
        }
        start = 0;
    }



}
