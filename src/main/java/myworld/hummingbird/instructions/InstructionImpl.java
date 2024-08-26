package myworld.hummingbird.instructions;

import myworld.hummingbird.Opcode;

public abstract class InstructionImpl {

    protected final Opcode ins;
    protected final int ip;
    protected final InstructionImpl next;

    public InstructionImpl(Opcode ins, int ip, InstructionImpl next){
        this.ins = ins;
        this.ip = ip;
        this.next = next;
    }

    public abstract int apply(long[] reg, int regOffset, int ip, InstructionImpl[] instructions);
}
