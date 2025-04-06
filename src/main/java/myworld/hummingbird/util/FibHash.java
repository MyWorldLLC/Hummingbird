package myworld.hummingbird.util;

/**
 * Fast 32-bit integer hashing based on https://probablydance.com/2018/06/16/fibonacci-hashing-the-optimization-that-the-world-forgot-or-a-better-alternative-to-integer-modulo/
 */
public class FibHash {

    public static int hash(int key){
        var fib = 1140071481932319848L;
        var hash = fib * key;
        hash ^= hash >> 32; // Mix in the high bits
        return (int) hash;
    }

    /**
     * @param hash hash code to map to the slot count range
     * @param slotCount must be a power of two
     */
    public static int hashToRange(int hash, int slotCount){
        return hash & (slotCount - 1);
    }

    public static int nextPowerOf2(int n){
        // Round up to nearest power of two
        var b = Integer.highestOneBit(n);
        return n > b ? b << 1 : b;
    }
}
