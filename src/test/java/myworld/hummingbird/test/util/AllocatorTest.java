package myworld.hummingbird.test.util;

import myworld.hummingbird.Executable;
import myworld.hummingbird.HummingbirdVM;
import myworld.hummingbird.MemoryLimits;
import myworld.hummingbird.util.Allocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static myworld.hummingbird.HummingbirdVM.NULL;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AllocatorTest {

    private HummingbirdVM vm;
    private Allocator allocator;

    @BeforeEach
    public void setupAllocator(){
        vm = new HummingbirdVM(Executable.builder().build(), new MemoryLimits(100, 0));
        allocator = new Allocator(vm, 10);
    }

    @Test
    public void allocateOnce(){
        var ptr = allocator.malloc(1);
        assertNotEquals(NULL, ptr, "Allocated pointer must not be null");
    }
}
