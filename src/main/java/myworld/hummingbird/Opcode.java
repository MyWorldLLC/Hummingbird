package myworld.hummingbird;

import myworld.hummingbird.instructions.OpcodeImpl;

public record Opcode(OpcodeImpl impl, int dst, int src, int extra, int extra1){

    public Opcode(OpcodeImpl impl, int dst, int src){
        this(impl, dst, src, 0, 0);
    }

    public Opcode(OpcodeImpl impl, int dst, int src, int extra){
        this(impl, dst, src, extra, 0);
    }

    public Opcode(OpcodeImpl impl, int dst){
        this(impl, dst, 0, 0);
    }

    public Opcode(OpcodeImpl impl){
        this(impl, 0, 0, 0);
    }

}
