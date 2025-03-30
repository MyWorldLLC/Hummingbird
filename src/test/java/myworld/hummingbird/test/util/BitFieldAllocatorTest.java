package myworld.hummingbird.test.util;

import myworld.hummingbird.util.BitFieldAllocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static myworld.hummingbird.util.BitFieldAllocator.ALLOC_FAILURE;
import static org.junit.jupiter.api.Assertions.*;

public class BitFieldAllocatorTest {

    private BitFieldAllocator allocator;

    @BeforeEach
    public void setupBitField(){
        allocator = new BitFieldAllocator(128);
    }

    @Test
    public void allocateCheckAndFree(){
        var ptr = allocator.allocate();
        assertNotEquals(ALLOC_FAILURE, ptr, "Allocation failed");

        assertTrue(allocator.isAllocated(ptr), "Allocator reports pointer is not allocated");

        allocator.free(ptr);

        assertFalse(allocator.isAllocated(ptr), "Allocator reports pointer is allocated after free");
    }

    @Test
    public void allocationFailure(){
        var allocator = new BitFieldAllocator(64);
        for(int i = 0; i < 64; i++){
            allocator.allocate();
        }
        var ptr = allocator.allocate();
        assertEquals(ALLOC_FAILURE, ptr, "Allocator claims success when it did not have capacity for allocation");
    }
}
