package myworld.hummingbird;

import myworld.hummingbird.instructions.OpcodeImpl;

public record Opcode(int opcode, int dst, int src, int extra, int extra1, OpcodeImpl impl){
    public Opcode(int opcode, int dst, int src){
        this(opcode, dst, src, 0);
    }

    public Opcode(int opcode, int dst, int src, int extra, int extra1){
        this(opcode, dst, src, extra, extra1, null);
    }

    public Opcode(int opcode, int dst, int src, int extra){
        this(opcode, dst, src, extra, 0, null);
    }

    public Opcode(int opcode, int dst){
        this(opcode, dst, 0, 0, 0, null);
    }

    public Opcode(int opcode){
        this(opcode, 0, 0, 0, 0, null);
    }

}
