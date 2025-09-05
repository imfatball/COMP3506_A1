// @edu:student-assignment

package uq.comp3506.a1.structures;
import java.lang.Math;
/**
 * Supplied by the COMP3506/7505 teaching team, Semester 2, 2025.
 */


public class BitVector {

    // The number of bits per integer stored
    public static final int BitsPerElement = 64;

    /**
     * The number of "active" bits that can be stored in this bitvector
     */
    private final long size;

    /**
     * The total number of bits allocated in the data array
     */
    private final long capacity;

    /**
     * We use 'long' instead of 'int' to store elements because it can fit
     * 64 bits instead of 32
     */
    private long[] data;

    /**
     * Constructs a bitvector, pre-allocating enough memory to store `size` bits
     */
    public BitVector(long size) {
        if (size < 0) throw new IllegalArgumentException("size < 0");
        this.size = size;
        this.capacity = Math.ceilDiv(size,BitsPerElement) * BitsPerElement;
        int words = (int) (capacity / BitsPerElement);
        data = new long[words];

    }

    /**
     * Returns the total number of bits that can be used
     */
    public long size() {
        return size;
    }

    /**
     * Returns the total number of bits allocated in the data array
     */
    public long capacity() {
        return capacity;
    }

    private void checkBounds(long idx) {
        if (idx < 0 || idx >= size) {
            throw new IndexOutOfBoundsException("Index " + idx + " is out of bounds.");
        }
    }

    private int index_word(long idx){
        return (int) (idx/BitsPerElement);
    }

    private long index_mask(long idx){
        long offset = idx%BitsPerElement;
        return 1L << offset;
    }

    /**
     * calculates the mask to zero everything past the size in the last word
     * @return
     */
    private long getTailMask() {
        int unused_bits = (int) (size%BitsPerElement);
        long mask;
        if (unused_bits == 0) {
            return ~0L;     // keep the whole last word
        } else {
            return (1L << unused_bits) - 1L;
        }
    }
    /**
     * Returns the value of the bit at index ix
     * If the index is out of bounds, you should throw an IndexOutOfBoundsException
     */
    public boolean get(long ix) {
        checkBounds(ix);
        return (data[index_word(ix)] & index_mask(ix)) != 0;
    }

    /**
     * Set the bit at index ix
     * If the index is out of bounds, you should throw an IndexOutOfBoundsException
     */
    public void set(long ix) {
        checkBounds(ix);
        data[index_word(ix)] |= index_mask(ix);
    }

    /**
     * Unset the bit at index ix
     * If the index is out of bounds, you should throw an IndexOutOfBoundsException
     */
    public void unset(long ix) {
        checkBounds(ix);
        data[index_word(ix)] &= ~index_mask(ix);

    }

    /**
     * Convert the BitVector to its complement
     * That means, all 1's become 0's and all 0's become 1's
     */
    public void complement() {
        if(size == 0){
            return;
        }
        for (int i = 0; i < capacity/BitsPerElement; i++) {
            data[i] = ~data[i];
        }
        long mask = getTailMask();
        data[(int) capacity/BitsPerElement -1] &= mask;

    }



    /**
     * Shift the bits `dist` positions
     * If dist is positive, this is a left shift, assuming the least significant
     * bit is the rightmost bit. So, consider you have a 4 element bitvector:
     * Indexes:  3 2 1 0
     * Elements: 1 1 0 1
     * Doing a shift(2) would yield:
     * Indexes:  3 2 1 0
     * Elements: 0 1 0 0
     *             ^--- This bit was previously at index 0
     *           ^----- This bit was previously at index 1
     *
     * Don't forget that you must also handle negative values of dist, and
     * these will invoke a right shift.
     *
     * The bits that "fall off" are always replaced with 0's.
     */
    public void shift(long dist) {
        if (dist == 0 || size == 0) {
            return;

        }
        if (dist >= size || dist <= -size) {
            for (int i = 0; i < capacity/BitsPerElement; i++) {
                data[i] = 0L;
            }
            return;
        }

        int words = (int) (capacity/BitsPerElement);

        long[] temp = new long[(int) (words)];
        long[] source = data;

        if(dist > 0) { // left shift

            int wordShift = (int) (dist/BitsPerElement);
            int bitShift = (int) (dist%BitsPerElement);

            for (int i = 0; i < words; i++) {
                if (i + wordShift >= 0 && i + wordShift  < words) {
                    temp[i + wordShift] |= (source[i] << bitShift);
                    if(bitShift !=0) {
                        if (i + wordShift + 1 >= 0 && i + wordShift + 1 < words) {
                            temp[i + wordShift + 1] |= source[i] >>> (BitsPerElement - bitShift); //unsigned right shift for zeros on the left
                        }
                    }

                }
            }

        }else{
            int wordShift = (int) (-dist/BitsPerElement);
            int bitShift = (int) (-dist%BitsPerElement);
            for (int i = 0; i < words; i++) {
                if (i - wordShift >=0 && i - wordShift < words) {
                    temp[i - wordShift] |= (source[i] >>> bitShift); //unsigned right shift for zeros on the left
                }
                if(bitShift != 0){
                    if(i - wordShift - 1 >=0 && i - wordShift - 1 < words){
                        temp[i - wordShift - 1] |= (source[i] << (BitsPerElement - bitShift));
                    }
                }
            }

        }
        if(words > 0){
            temp[words -1] &= getTailMask();
        }
        data = temp;

    }
 
    /**
     * Rotate the bits `dist` positions
     * If dist is positive, this is a left rotation, assuming the least significant
     * bit is the rightmost bit. So, consider you have a 5 element bitvector:
     * Indexes:  4 3 2 1 0
     * Elements: 1 1 1 0 1
     * Doing a rotate(2) would yield:
     * Indexes:  4 3 2 1 0
     * Elements: 1 0 1 1 1
     *                 ^This bit was previously at index 4
     *             ^--- This bit was previously at index 1
     *           ^----- This bit was previously at index 2
     * As you can see, it operates the same as the shift, but the bits that
     * are moved "off the end" of the vector wrap back around to the beginning.
     *
     * Don't forget that you must also handle negative values of dist, and
     * these will invoke a right shift.
     */
    public void rotate(long dist) {
        if (size == 0) {
            return;
        }

        // brings the dist down to in between 0 and size, negatives are just size - norm_dist
        long norm_dist = dist % size;
        if (dist < 0) {
            norm_dist += size;
        }
        if (norm_dist == 0) return;

        int words = (int) (capacity / BitsPerElement);
        long[] temp = new long[(int) (words)];
        long[] source = data;


        for (long i = 0; i < size; i++) {
            int sourceWords = (int) (i / BitsPerElement);
            int sourceOff  = (int) (i % BitsPerElement);
            if (((source[sourceWords] >>> sourceOff) & 1L) != 0L) {
                long j = (i + norm_dist) % size;
                int dstWord = (int) (j / BitsPerElement);
                int dstOff  = (int) (j % BitsPerElement);
                temp[dstWord] |= (1L << dstOff);
            }
        }

        if(words > 0){
            temp[words -1] &= getTailMask();
        }
        data = temp;


    }

    /**
     * COMP7505 only (COMP3506 may do this for fun)
     * Returns the number of bits that are set to 1 across the entire bitvector
     */
    public long popcount() {

        return -1;
    }

    /**
     * COMP7505 only (COMP3506 may do this for fun)
     * Returns the length of the longest run of bits that are set (1) across
     * the entire bitvector
     */
    public long runcount() {

        return -1;
    }

}
