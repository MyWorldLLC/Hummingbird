package myworld.hummingbird.util;

import myworld.hummingbird.HummingbirdVM;

import static myworld.hummingbird.HummingbirdVM.NULL;

public class RingAllocator {

    public static final int NALLOC = 1024;
    public static final int DEFAULT_INITIAL_SIZE = -1;
    public static final int SIZE_OFFSET = 4;

    private final HummingbirdVM vm;
    private final int baseAddress;

    private FreeBlock anchor;

    public RingAllocator(HummingbirdVM vm, int baseAddress){
        this(vm, baseAddress, DEFAULT_INITIAL_SIZE);
    }

    public RingAllocator(HummingbirdVM vm, int baseAddress, int initialSize){
        this.vm = vm;
        this.baseAddress = baseAddress;

        anchor = new FreeBlock();
        anchor.dataPtr = baseAddress + SIZE_OFFSET;
        anchor.next = anchor;
        if(initialSize == DEFAULT_INITIAL_SIZE){
            setBlockSize(anchor, vm.memorySize() - baseAddress - SIZE_OFFSET);
        }else{
            setBlockSize(anchor, initialSize);
        }
    }

    public synchronized int malloc(int nbytes){

        var prior = anchor;
        var block = anchor.next;
        do {

            var size = blockSize(block);

            if(size == nbytes && !(block == anchor && block.next == anchor)){
                // Exact fit, remove block from free list and return its data pointer
                prior.next = block.next;
                if(block == anchor){
                    anchor = block.next;
                }
                return block.dataPtr;
            }else if(size > nbytes + SIZE_OFFSET){
                // Split the block

                // There's room in this block to claim the memory we need
                // and also leave behind a newly created block that's a fragment
                // of this one.
                var newBlock = new FreeBlock();

                newBlock.dataPtr = block.dataPtr + nbytes + SIZE_OFFSET;
                setBlockSize(newBlock, blockSize(block) - (nbytes + SIZE_OFFSET));
                setBlockSize(block, nbytes);

                prior.next = newBlock;
                newBlock.next = block.next;

                if(block == anchor){
                    anchor = newBlock;
                }

                return block.dataPtr;
            }

            prior = block;
            block = block.next;
        }while(block != anchor);

        // We looped around and couldn't find a suitable block, so request
        // more memory from the VM.

        var newCore = morecore(nbytes);
        if(newCore != null){
            return malloc(nbytes);
        }

        return NULL;
    }

    public synchronized void free(int ptr){

        if(ptr < baseAddress){
            return;
        }

        var block = anchor;
        do {
            block = block.next;
        } while(!insertionPoint(block, ptr) && block != anchor);

        var size = blockSize(ptr);

        if(block.dataPtr + blockSize(block) + SIZE_OFFSET == ptr){
            setBlockSize(block, blockSize(block) + SIZE_OFFSET + size);
        }else{
            // block can't merge with the new block inserted by this pointer
            var newBlock = new FreeBlock();
            newBlock.dataPtr = ptr;
            newBlock.next = block.next;
            block.next = newBlock;
            block = newBlock;
        }

        // block is either original block we found merged with the newly freed block,
        // or it's the new block that was just inserted
        if(block.dataPtr + blockSize(block) + SIZE_OFFSET == block.next.dataPtr){
            setBlockSize(block, blockSize(block) + SIZE_OFFSET + blockSize(block.next));
            block.next = block.next.next;
            anchor = block;
        }

    }

    private boolean insertionPoint(FreeBlock block, int ptr){
        // Insert when the ptr is either between the pointers of this block and the next, or
        // when the pointer is above this block and the next block's pointer is less than the prior
        // (meaning we've come to the wraparound block).

        // Note that the '=' condition should never be able to happen, but we use it to break from the
        // loop in case of a bad pointer being passed
        return block.dataPtr <= ptr && (ptr <= block.next.dataPtr || block.next.dataPtr <= block.dataPtr);
    }

    private int blockSize(int ptr){
        return vm.readInt(ptr - SIZE_OFFSET);
    }

    private int blockSize(FreeBlock block){
        return blockSize(block.dataPtr);
    }

    private void setBlockSize(FreeBlock block, int size){
        vm.writeInt(block.dataPtr - SIZE_OFFSET, size);
    }

    private FreeBlock morecore(int nbytes){

        nbytes = Math.max(nbytes + SIZE_OFFSET, NALLOC);

        int ptr = sbrk(nbytes);
        if(ptr == -1){
            return null;
        }

        var newBlock = new FreeBlock();
        newBlock.dataPtr = ptr + SIZE_OFFSET;
        setBlockSize(newBlock, nbytes);

        free(newBlock.dataPtr);
        return newBlock;
    }

    public synchronized int sbrk(int moreBytes){
        int oldSize = vm.memorySize();
        if(moreBytes == 0){
            return oldSize;
        }
        int newSize = vm.resize(oldSize + moreBytes);
        if(newSize == oldSize){
            return -1;
        }
        return oldSize;
    }

    public synchronized int freeSpace(){
        int size = 0;
        var block = anchor;
        do {
            size += blockSize(block);
            block = block.next;
        }while (block != anchor);
        return size;
    }

    public synchronized int countFreeBlocks(){
        int count = 0;
        var block = anchor;
        do {
            count++;
            block = block.next;
        }while (block != anchor);
        return count;
    }

    public synchronized int[] blockSizes(){
        var blocks = countFreeBlocks();
        var sizes = new int[blocks];
        var block = anchor;
        for(int i = 0; i < blocks; i++){
            sizes[i] = blockSize(block);
            block = block.next;
        }
        return sizes;
    }

    public synchronized int[] freeBlocks(){
        var blocks = countFreeBlocks();
        var pointers = new int[blocks];
        var block = anchor;
        for(int i = 0; i < blocks; i++){
            pointers[i] = block.dataPtr;
            block = block.next;
        }
        return pointers;
    }

    private static class FreeBlock {
        FreeBlock next;
        int dataPtr;
    }
}
