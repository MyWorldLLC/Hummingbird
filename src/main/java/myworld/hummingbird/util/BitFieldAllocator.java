package myworld.hummingbird.util;

/**
 * Allocator that uses a bitfield to track the state of the resource. This doesn't require any access
 * to the resource being allocated from. This is meant to be used to track free array indices, so only single
 * element allocations are supported.
 */
public class BitFieldAllocator {

    public static final int ALLOC_FAILURE = -1;

    private final BitField state;
    private int lastFree = 0;

    public BitFieldAllocator(int initialSize){
        state = new BitField(initialSize);
    }

    public int allocate(){
        if(lastFree != -1){
            state.set(lastFree);
            var ptr = lastFree;
            lastFree = -1;
            return ptr;
        }

        for(int i = 0; i < state.wordCount(); i++){
            var word = state.word(i);
            var freeBit = Long.highestOneBit(~word);
            if(freeBit != 0){
                var ptr = i * 64 + Long.numberOfLeadingZeros(freeBit);
                state.set(ptr);
                return ptr;
            }
        }
        return ALLOC_FAILURE;
    }

    public boolean isAllocated(int ptr){
        return state.isSet(ptr);
    }

    public void free(int ptr){
        state.clear(ptr);
        lastFree = ptr;
    }

}
