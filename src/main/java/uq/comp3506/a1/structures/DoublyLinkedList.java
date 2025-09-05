// @edu:student-assignment

package uq.comp3506.a1.structures;

/**
 * Supplied by the COMP3506/7505 teaching team, Semester 2, 2025.
 * A custom doubly linked list class supporting generic types.
 * The Node class is a nested class, as Node objects will never
 * be exposed to users of the DoublyLinkedList object.
 *
 * @param <T> The type of elements stored in the list
 */

public class DoublyLinkedList<T> implements ListInterface<T> {

    private Node head;
    private Node tail;
    private int size = 0;

    /**
     * Constructs an empty linked list
     */
    public DoublyLinkedList() {
    }

    /**
     * Returns the number of elements contained in the DLL.
     */
    @Override // See ListInterface
    public int size() {
        return size;
    }

    /**
     * Returns true if there are no elements in the DLL
     */
    @Override // See ListInterface
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Checks the bounds on the DLL
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
     * Returns the element at idx if it exists, throwing an exception if the
     * index is out of bounds (via checkBounds)
     */
    @Override // See ListInterface
    public T get(int idx) {
        checkBounds(idx, true);
        int curIdx = 0;
        Node cur = this.head;
        while (cur != null && curIdx != idx) {
            cur = cur.getNext();
            curIdx++;
        }
        return cur.getData();
    }


    /**
     * Overwrites the "old" value at idx to t, and returns the "old" value;
     * throws an exception if the index is out of bounds (via checkBounds).
     */
    @Override // See ListInterface
    public T set(int idx, T t) {
        checkBounds(idx, true);
        Node cur = head;
        int curIdx = 0;
        while (cur != null && curIdx != idx) {
            cur = cur.getNext();
            curIdx++;
        }
        T oldData = cur.getData();
        cur.setData(t);
        return oldData;
    }

    /**
     * Adds an element to the end of the DLL. Returns true if successful,
     * false otherwise
     */
    @Override // See ListInterface
    public boolean append(T data) {
        Node newNode = new Node(data);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            tail.setNext(newNode);
            newNode.setPrev(tail);
            tail = newNode;
        }
        size += 1;
        return true;
    }

    /**
     * Adds an element to the start of the DLL. Returns true if successful,
     * false otherwise
     */
    @Override
    public boolean prepend(T data) {
        Node newNode = new Node(data);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            newNode.setNext(head);
            head.setPrev(newNode);
            head = newNode;
        }
        size += 1;
        return true;
    }

    /**
     * Adds an element to the DLL at the given index idx. Returns true if
     * successful, false otherwise. If idx is out of bounds, throws an
     * exception via checkBounds.
     * Note that adding an element to the first index beyond the final
     * element is valid -- that is, we can call add with idx = this.size
     * which triggers an append.
     */
    @Override // See ListInterface
    public boolean add(int idx, T data) {
        checkBounds(idx, false);
        if (idx == 0) { // Case 1: The index is the head
            return this.prepend(data);
        } else if (idx == this.size) { // Case 2: The index is the tail
            return this.append(data);
        }
        int curIdx = 0;
        Node cur = this.head;
        while (cur != null && curIdx != idx) {
            cur = cur.getNext();
            curIdx++;
        }

        Node newNode = new Node(data);
        newNode.setPrev(cur.getPrev());
        newNode.setNext(cur);
        cur.getPrev().setNext(newNode);
        cur.setPrev(newNode);
        size += 1;
        return true;
    }

    /**
     * Returns the data in head if it exists, null otherwise.
     */
    public T getFirst() {
        if (isEmpty()) {
            return null;
        }
        return head.getData();
    }

    /**
     * Returns the data in the tail if it exists, null otherwise.
     */
    public T getLast() {
        if (isEmpty()) {
            return null;
        }
        return tail.getData();
    }

    /**
     * Removes and returns the element at idx if the idx is valid; throws an
     * exception if the idx is not valid.
     */
    @Override // See ListInterface
    public T remove(int idx) {
        checkBounds(idx, true);
        if (idx == 0) {
            Node node = head;
            head = head.getNext();
            T data = node.getData();
            if (head != null) {
                head.setPrev(null);
            } else {
                // list became empty
                tail = null;
            }
            size -= 1;
            return data;
        }
        Node cur = head;
        int curIdx = 0;
        while (cur != null && curIdx != idx) {
            cur = cur.getNext();
            curIdx++;
        }
        Node prev = cur.getPrev();
        Node next = cur.getNext();
        prev.setNext(next);
        if (next != null) {
            next.setPrev(prev);
        } else {
            tail = prev;
        }
        size -= 1;
        return cur.getData();
    }

    /**
     * Finds and removes the first matching element, returning true if
     * successful and false otherwise.
     * Does nothing to the list if there is no such element
     */
    public boolean removeFirst(T t) {
        if (isEmpty()) {
            return false;
        }
        Node cur = head;
        while (cur != null && cur.getData() != t) {
            cur = cur.getNext();
        }
        if (cur == null) {
            return false;
        }
        if (cur == head) {
            head = cur.getNext();
            if (head != null) {
                head.setPrev(null);
            } else {
                tail = null; // list becomes empty
            }
        } else if (cur == tail) {
            tail = cur.getPrev();
            tail.setNext(null);
        } else {
            cur.getPrev().setNext(cur.getNext());
            cur.getNext().setPrev(cur.getPrev());
        }
        size -= 1; // FIX
        return true;
    }

    /**
     * Reset to an empty linked list.
     */
    @Override // See ListInterface
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * The nested Node class.
     * data: The data payload
     * prev: The reference to the previous node in the list
     * next: The reference to the next node in the list
     */
    private class Node {
        private T data;
        private Node prev;
        private Node next;

        /**
         * Constructs the node type with the given data payload
         *
         * @param data The payload data to store inside this node
         */
        public Node(T data) {
            this.data = data;
            this.next = null;
            this.prev = null;
        }

        public T getData() {
            return data;
        }

        public void setData(T newData) {
            this.data = newData;
        }

        public Node getPrev() {
            return prev;
        }

        public void setPrev(Node prev) {
            this.prev = prev;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

    } // End of nested Node class

} // End DoublyLinkedList Class

