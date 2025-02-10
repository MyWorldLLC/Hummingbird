package myworld.hummingbird;

import myworld.hummingbird.instructions.OpcodeImpl;

public record Opcode(OpcodeImpl impl, int opFlags, int dst, int src, int extra, int extra1){

    public Opcode(OpcodeImpl impl, int opFlags, int dst, int src){
        this(impl, opFlags, dst, src, 0, 0);
    }

    public Opcode(OpcodeImpl impl, int opFlags, int dst, int src, int extra){
        this(impl, opFlags, dst, src, extra, 0);
    }

    public Opcode(OpcodeImpl impl, int opFlags, int dst){
        this(impl, opFlags, dst, 0, 0);
    }

    public Opcode(OpcodeImpl impl, int opFlags){
        this(impl, opFlags, 0, 0);
    }

}
