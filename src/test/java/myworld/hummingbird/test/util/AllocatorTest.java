package myworld.hummingbird.test.util;

import myworld.hummingbird.Executable;
import myworld.hummingbird.HummingbirdVM;
import myworld.hummingbird.MemoryLimits;
import myworld.hummingbird.util.Allocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static myworld.hummingbird.HummingbirdVM.NULL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AllocatorTest {

    private Allocator.HeaderStruct header;
    private HummingbirdVM vm;
    private Allocator allocator;

    @BeforeEach
    public void setupAllocator(){
        vm = new HummingbirdVM(Executable.builder().build(), new MemoryLimits(100, 0));
        header = new Allocator.HeaderStruct(vm);
        allocator = new Allocator(vm, 0);
    }

    @Test
    public void allocateOnce(){
        var ptr = allocator.malloc(1);
        assertNotEquals(NULL, ptr, "Allocated pointer must not be null");
        assertNotEquals(0, allocator.freeSpace(), "Allocator must have free space remaining");
    }

    @Test
    public void allocateMax(){
        var size = 100 - 2 * header.sizeOf();
        var ptr = allocator.malloc(size);
        assertNotEquals(NULL, ptr, "Allocated pointer must not be null");
        ptr = allocator.malloc(1);
        assertEquals(NULL, ptr, "Allocation past limit must return null");
    }

    @Test
    public void allocateMaxWithoutHeaderSpace(){
        var ptr = allocator.malloc(100);
        assertEquals(NULL, ptr, "Allocation must fail if space is not left for header");
    }

    @Test
    public void freeAndReallocateMax(){
        var ptr = allocator.malloc(100 - 2 * header.sizeOf());
        assertNotEquals(NULL, ptr, "Initial allocation must succeed");
        allocator.free(ptr);
        ptr = allocator.malloc(100 - 2 * header.sizeOf());
        assertNotEquals(NULL, ptr, "Subsequent allocation must succeed");
        assertEquals(0, allocator.freeSpace(), "Allocator must have no more free space available");
    }

    @Test
    public void mergeAdjacentFreeBlocks(){
        var p1 = allocator.malloc(10);
        var p2 = allocator.malloc(10);
        var p3 = allocator.malloc(10);

        allocator.free(p1);
        allocator.free(p2);
        allocator.free(p3);

        assertEquals(1, allocator.countFreeBlocks(), "Allocator must merge free blocks");
        assertEquals(100 - header.sizeOf(), allocator.freeSpace(), "Space must be fully free");
    }

    @Test
    public void delegateTest(){
        var subBase = allocator.malloc(50);
        var subAllocator = new Allocator(vm, subBase, allocator, 50);

        assertEquals(34, subAllocator.freeSpace(), "Suballocator correctly reports initial free space");
        var ptr = subAllocator.malloc(15);
        assertNotEquals(NULL, ptr, "Suballocator correctly allocates a pointer");
        assertEquals(34 - (15 + header.sizeOf()), subAllocator.freeSpace(), "Suballocator correctly reports post-allocation free space");
        subAllocator.free(ptr);
        assertEquals(42, subAllocator.freeSpace(), "Suballocator correctly reports final free space");
    }
}
