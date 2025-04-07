package myworld.hummingbird.util;

import java.util.Objects;

import static myworld.hummingbird.HummingbirdVM.NULL;

public class FreeBlock {
    protected FreeBlock previous;
    protected FreeBlock next;
    protected int ptr;
    protected int size;

    public FreeBlock(int ptr, int size){
        this.ptr = ptr;
        this.size = size;
    }

    public static FreeBlock head(int ptr, int initialSize){
        var head = new FreeBlock(ptr, 0);
        var tail = new FreeBlock(ptr + 1, initialSize - 1);
        head.setNext(tail);
        tail.setPrevious(head);
        return head;
    }

    public final boolean isHead(){
        return previous == null;
    }

    public final boolean isTail(){
        return next == null;
    }

    public void setPrevious(FreeBlock previous) {
        this.previous = previous;
    }

    public void setNext(FreeBlock next) {
        this.next = next;
    }

    public FreeBlock next(){
        return next;
    }

    public FreeBlock previous(){
        return previous;
    }

    public void setPtr(int ptr){
        this.ptr = ptr;
    }

    public int ptr(){
        return ptr;
    }

    public int size(){
        return size;
    }

    public void setSize(int size){
        if(isHead()){
            throw new IllegalStateException("Head size must remain 0: " + Objects.toIdentityString(this));
        }
        this.size = size;
    }

    public void coalesceSize(int size){
        if(isHead()){
            throw new IllegalStateException("Head size must remain 0: " + Objects.toIdentityString(this));
        }
        this.size += size;
    }

    public int findOrSplit(int size){
        if(!isHead()){
            throw new IllegalStateException("Free list can only be modified from the head block");
        }

        var block = this.next();
        while(block != null){
            if(block.size() == size){
                // It's an exact fit - just remove the block and return its pointer

                block.previous().setNext(block.next());
                if(!block.isTail()){
                    block.next().setPrevious(block.previous());
                }
                return block.ptr();
            }else if(block.size() > size){
                // Not an exact size - split the block
                var ptr = block.ptr();
                block.setPtr(ptr + size);
                block.setSize(block.size() - size);

                return ptr;
            }

            block = block.next();
        }
        return NULL;
    }

    public void insert(int ptr, int size){
        if(!isHead()){
            throw new IllegalStateException("Free list can only be modified from the head block");
        }

        if(ptr <= ptr()){
            return; // Invalid pointer - it comes before the head block starts
        }

        var prior = this.next();
        if(prior == null){
            // Sometimes all the free blocks are consumed and we're left with no free blocks left.
            // When this happens, just insert a new block after the head without attempting coalescing (since
            // there's nothing to coalesce).
            prior = new FreeBlock(ptr, size);
            prior.setPrevious(this);
            this.setNext(prior);
            return;
        }
        while(true){

            if(prior.coalesce(ptr, size)){
                return;
            }

            if(ptr < prior.ptr()){
                // Found insertion point before the prior checked block, but the prior cannot coalesce

                if(!prior.previous().coalesce(ptr, size)){
                    var block = new FreeBlock(ptr, size);
                    prior.previous().setNext(block);
                    block.setPrevious(prior.previous());

                    prior.setPrevious(block);
                    block.setNext(prior);
                }
                return;
            }

            if(prior.isTail()){
                // The prior block is the tail, so add a new tail
                var block = new FreeBlock(ptr, size);
                prior.setNext(block);
                block.setPrevious(prior);
                break;
            }

            prior = prior.next();
        }

    }

    public boolean coalesce(int ptr, int size){
        if(isHead()){
            return false;
        }

        if(ptr + size == this.ptr || this.ptr + this.size == ptr){
            // Valid coalesce
            this.setPtr(Math.min(this.ptr, ptr));
            this.coalesceSize(size);

            // Now attempt to coalesce this newly expanded block into the surrounding blocks
            var coalesced = this;
            if(!previous.isHead() && previous.ptr() + previous.size() == this.ptr()){
                // Coalesce to trailing edge of leading block
                previous.setNext(next);
                if(!isTail()){
                    next.setPrevious(previous);
                }
                previous.coalesceSize(this.size());
                coalesced = previous;
            }
            if(!coalesced.isTail()){
                if(coalesced.ptr() + coalesced.size() == coalesced.next().ptr()){
                    // Coalesce to leading edge of trailing block
                    coalesced.next().setPtr(coalesced.ptr());
                    coalesced.next().coalesceSize(coalesced.size());
                    coalesced.next().setPrevious(coalesced.previous());
                    coalesced.previous().setNext(coalesced.next());
                }
            }
            return true;
        }
        return false;
    }

    public int freeSpace(){
        if(!isHead()){
            throw new IllegalStateException("Must call freeSpace() from head");
        }
        var space = 0;
        var block = next();
        while(block != null){
            space += block.size();
            block = block.next();
        }
        return space;
    }

    public int countFreeBlocks(){
        if(!isHead()){
            throw new IllegalStateException("Must call countFreeBlocks() from head");
        }
        var blocks = 0;
        var block = this;
        while(block != null){
            blocks++;
            block = block.next();
        }
        return blocks;
    }

    public int[] freeBlockSizes(){
        if(!isHead()){
            throw new IllegalStateException("Must call freeBlockSizes() from head");
        }
        var count = countFreeBlocks();
        var sizes = new int[count];
        var block = this;
        for(int i = 0; i < sizes.length; i++){
            sizes[i] = block.size();
            block = block.next();
        }
        return sizes;
    }

    public int[] freeBlockPointers(){
        if(!isHead()){
            throw new IllegalStateException("Must call freeBlockPointers() from head");
        }
        var count = countFreeBlocks();
        var pointers = new int[count];
        var block = this;
        for(int i = 0; i < pointers.length; i++){
            pointers[i] = block.ptr();
            block = block.next();
        }
        return pointers;
    }
}
