package myworld.hummingbird.util;

import myworld.hummingbird.HummingbirdVM;

public class StructAccessor {

    private final HummingbirdVM vm;
    private final Struct struct;

    public StructAccessor(HummingbirdVM vm, Struct struct){
        this.vm = vm;
        this.struct = struct;
    }

    public int readInt(int basePtr, int field){
        return vm.memory.getInt(basePtr + struct.offsetOf(field));
    }

    public void writeInt(int basePtr, int field, int value){
        vm.memory.putInt(basePtr + struct.offsetOf(field), value);
    }

    public Struct struct(){
        return struct;
    }

}
