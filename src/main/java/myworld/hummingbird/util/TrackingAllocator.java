package myworld.hummingbird.util;

import myworld.hummingbird.HummingbirdVM;

import static myworld.hummingbird.HummingbirdVM.NULL;

public class TrackingAllocator {

    public static final int DEFAULT_INITIAL_SIZE = -1;
    public static final int DEFAULT_CORE_INCREMENT = 1024;

    private final HummingbirdVM vm;
    private int coreIncrement;
    private IntHashTable allocated;
    private final FreeBlock freeList;

    public TrackingAllocator(HummingbirdVM vm, int baseAddress){
        this(vm, baseAddress, DEFAULT_INITIAL_SIZE, DEFAULT_CORE_INCREMENT);
    }

    public TrackingAllocator(HummingbirdVM vm, int baseAddress, int initialSize){
        this(vm, baseAddress, initialSize, DEFAULT_CORE_INCREMENT);
    }

    public TrackingAllocator(HummingbirdVM vm, int baseAddress, int initialSize, int coreIncrement){
        this.vm = vm;
        allocated = new IntHashTable(100, 0.75f);
        this.coreIncrement = coreIncrement;

        if(initialSize == DEFAULT_INITIAL_SIZE){
            initialSize = vm.memorySize() - baseAddress;
        }

        freeList = FreeBlock.head(baseAddress, initialSize);
    }

    private boolean moreCore(int size){

        size = Math.max(size , coreIncrement);

        int newBlock = sbrk(size);
        if(newBlock == -1){
            return false;
        }

        freeList.insert(newBlock, size);
        return true;
    }

    private int sbrk(int moreBytes){
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

    public synchronized int malloc(int size){
        if(size == 0){
            return NULL;
        }
        var ptr = freeList.findOrSplit(size);
        if(ptr == NULL){
            if(!moreCore(size)){
                return NULL;
            }
            // Retry after morecore() is called above
            // At this point we're either guaranteed success or
            // the VM has hit its memory limit
            ptr = freeList.findOrSplit(size);
        }
        if(ptr != NULL){
            allocated.insert(ptr, size);
        }
        return ptr;
    }

    public synchronized void free(int ptr){
        if(ptr == NULL){
            return;
        }

        var size = allocated.remove(ptr);
        if(size == 0){
            return; // Invalid pointer
        }

        freeList.insert(ptr, size);
    }

    public synchronized int freeSpace(){
        return freeList.freeSpace();
    }

    public synchronized int countFreeBlocks(){
        return freeList.countFreeBlocks();
    }

    public synchronized int[] freeBlockSizes(){
        return freeList.freeBlockSizes();
    }

    public synchronized int[] freeBlockPointers(){
        return freeList.freeBlockPointers();
    }

}
