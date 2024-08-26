package myworld.hummingbird.instructions;

import myworld.hummingbird.Opcode;

public class ConstImpl extends InstructionImpl {

    protected final long value;

    public ConstImpl(Opcode ins, int ip, InstructionImpl next, long value) {
        super(ins, ip, next);
        this.value = value;
    }

    @Override
    public int apply(long[] reg, int regOffset) {
        reg[regOffset + ins.dst()] = value;
        return next.apply(reg, regOffset);
    }
}
