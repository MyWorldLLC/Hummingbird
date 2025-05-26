package myworld.hummingbird.util.gc;

import myworld.hummingbird.ForeignFunction;
import myworld.hummingbird.HummingbirdVM;
import myworld.hummingbird.util.*;

public class GC {

    protected final HummingbirdVM vm;
    protected final TrackingAllocator allocator;
    protected final IntSet gcPointers;
    protected final GcObject objectDef;
    protected final StructAccessor gcAccessor;

    protected final ForeignFunction inc;
    protected final ForeignFunction dec;
    protected final ForeignFunction gcNew;

    public GC(HummingbirdVM vm, TrackingAllocator allocator){
        this.vm = vm;
        this.allocator = allocator;
        gcPointers = new IntSet(1024);
        objectDef = new GcObject();
        gcAccessor = new StructAccessor(vm, objectDef.struct());

        inc = (hvm, fiber) -> inc((int)fiber.registers[0]);
        dec = (hvm, fiber) -> dec((int)fiber.registers[0]);
        gcNew = (hvm, fiber) -> fiber.registers[0] = gcNew((int)fiber.registers[0]);

    }

    public ForeignFunction getInc(){
        return inc;
    }

    public ForeignFunction getDec(){
        return dec;
    }

    /**
     * Note: for performance reasons, this does not verify that
     * the passed pointer is a GC object before attempting to alter its references.
     * This will almost certainly result in memory corruption if passed a non-GC pointer.
     */
    public void dec(int ptr){
        var count = gcAccessor.readInt(ptr, objectDef.refCountField);
        count--;
        gcAccessor.writeInt(ptr, objectDef.refCountField, count);
        if(count == 0){
          gcFree(ptr);
        } else if(count == 1){
            collectCycle(ptr, new IntSet());
        }
    }

    /**
     * Note: for performance reasons, this does not verify that
     * the passed pointer is a GC object before attempting to alter its references.
     * This will result in memory corruption if passed a non-GC object.
     */
    public void inc(int ptr){
        var count = gcAccessor.readInt(ptr, objectDef.refCountField);
        count++;
        gcAccessor.writeInt(ptr, objectDef.refCountField, count);
    }

    public int gcNew(int size){
        var ptr = allocator.malloc(size);
        gcPointers.add(ptr);
        gcAccessor.writeInt(ptr, objectDef.sizeField, size);
        gcAccessor.writeInt(ptr, objectDef.refCountField, 1);
        return ptr;
    }

    public boolean isGcPtr(int ptr){
        return gcPointers.contains(ptr);
    }

    protected void gcFree(int ptr){
        gcPointers.remove(ptr);
        allocator.free(ptr);
    }

    protected void collectCycle(int ptr, IntSet collection){
        // Note: For cycle tracing we assume that every positive integer aligned
        // at 32-bit increments from the base pointer *may* be a pointer. Since pointers
        // are direct references into memory and must be positive, we ignore 0/negative values
        // since they are not valid pointers. Candidate pointers are checked against the set
        // of pointers allocated by gcNew().

        // It is assumed that the passed pointer is a GC object and that its reference count has just
        // been decremented to 1.

        var queue = new IntQueue();
        queue.push(ptr);
        collection.add(ptr);

        var cycleFound = false;
        while(!queue.isEmpty()){
            var p = queue.pop();

            var objSize = gcAccessor.readInt(p, objectDef.sizeField);
            for(var field = p + objectDef.struct().sizeOf(); field < objSize; field += Sizes.INT){
                var candidatePtr = gcAccessor.readInt(p, field);
                // This field is a gc pointer
                if(candidatePtr > 0 && gcPointers.contains(candidatePtr)){
                    if(candidatePtr == ptr){
                        // Cycle found!
                        cycleFound = true;
                        break;
                    } else if(gcAccessor.readInt(candidatePtr, objectDef.refCountField) == 1){
                        // We only trace through 1-count reference chains that start from the object
                        // that triggered the collection cycle.
                        queue.push(candidatePtr);
                        collection.add(p);
                    }
                }
            }
        }

        if(cycleFound){
            // Walk through all objects marked as part of the collection, decrementing the ref counts
            // of any gc objects referenced in their fields and triggering collections on them as appropriate.
            for(var p : collection.elements()){

                var objSize = gcAccessor.readInt(p, objectDef.sizeField);
                for(var field = p + objectDef.struct().sizeOf(); field < objSize; field += Sizes.INT){
                    var refPtr = gcAccessor.readInt(p, field);
                    // This field is a gc pointer
                    if(refPtr > 0 && gcPointers.contains(refPtr)){
                        dec(refPtr);
                    }
                }

                gcFree(p);
            }
        }
    }
}
