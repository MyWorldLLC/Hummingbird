package myworld.hummingbird.test.util;

import myworld.hummingbird.Executable;
import myworld.hummingbird.HummingbirdVM;
import myworld.hummingbird.MemoryLimits;
import myworld.hummingbird.util.TrackingAllocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static myworld.hummingbird.HummingbirdVM.NULL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TrackingAllocatorTest {
    private HummingbirdVM vm;
    private TrackingAllocator allocator;

    private int maxAllocation;
    private int fullyFree;

    @BeforeEach
    public void setupAllocator(){
        vm = new HummingbirdVM(Executable.builder().build(), new MemoryLimits(100, 0));
        allocator = new TrackingAllocator(vm, 10, 90);
        maxAllocation = 89;
        fullyFree = 89;
    }

    @Test
    public void allocateOnce(){
        var ptr = allocator.malloc(1);
        assertNotEquals(NULL, ptr, "Allocated pointer must not be null");
        assertNotEquals(0, allocator.freeSpace(), "Allocator must have free space remaining");
    }

    @Test
    public void allocateMax(){
        var ptr = allocator.malloc(maxAllocation);
        assertNotEquals(NULL, ptr, "Allocated pointer must not be null");
        ptr = allocator.malloc(1);
        assertEquals(NULL, ptr, "Allocation past limit must return null");
    }

    @Test
    public void allocateMaxWithoutHeaderSpace(){
        var ptr = allocator.malloc(90);
        assertEquals(NULL, ptr, "Allocation must fail if space is not left for head block");
    }

    @Test
    public void freeAndReallocateMax(){
        var ptr = allocator.malloc(maxAllocation);
        assertNotEquals(NULL, ptr, "Initial allocation must succeed");
        allocator.free(ptr);
        ptr = allocator.malloc(maxAllocation);
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

        assertEquals(2, allocator.countFreeBlocks(), "Allocator must merge free blocks");
        assertEquals(fullyFree, allocator.freeSpace(), "Space must be fully free");
    }
}
