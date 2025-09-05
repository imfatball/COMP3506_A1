// @edu:student-assignment

package uq.comp3506.a1.structures;

/**
 * Supplied by the COMP3506/7505 teaching team, Semester 2, 2025.
 * An abstract data type representing the ``list'' operations to represent
 * an ordered collection of elements of type T.
 */
public interface ListInterface<T> {

    /**
     * size is the logical size of the structure; that is, how many valid
     * elements are stored within.
     *
     * @return the number of elements stored inside the given structure
     */
    int size();

    /**
     * If size is non-zero, then this should return false, as the container
     * cannot be empty.
     *
     * @return true if empty, false otherwise
     */
    boolean isEmpty();

    /**
     * Appends t to the end of the list.
     *
     * @param t element to add
     * @return true if added, false otherwise
     */
    boolean append(T t);

    /**
     * Prepends t to the beginning of the list.
     *
     * @param t element to add
     * @return true if added, false otherwise
     */
    boolean prepend(T t);

    /**
     * Adds t to the list at the specific idx
     *
     * @param t   element to add
     * @param idx index to insert at
     * @return true if added, false otherwise
     * @throws IndexOutOfBoundsException if idx is out of bounds
     */
    boolean add(int idx, T t);

    /**
     * Returns the element at a given index
     *
     * @param idx index to access
     * @return element at the given index
     * @throws IndexOutOfBoundsException if idx is out of bounds
     */
    T get(int idx);

    /**
     * Overwrites the element at a given index and returns the old element
     *
     * @param t   element to add
     * @param idx index to access
     * @return element previously at idx
     * @throws IndexOutOfBoundsException if idx is out of bounds
     */
    T set(int idx, T t);

    /**
     * Removes and returns the element at a given index
     *
     * @param idx index to access
     * @return element removed from the given index
     * @throws IndexOutOfBoundsException if idx is out of bounds
     */
    T remove(int idx);

    /**
     * Finds and removes the first matching element
     * Does nothing to the list if there is no such element
     *
     * @return true if the element was present in the list; false otherwise
     */
    boolean removeFirst(T t);

    /**
     * Clears all elements from the list. That means, after calling clear(),
     * the return of size() should be 0, and the data structure should appear
     * to be "empty"
     */
    void clear();

}
