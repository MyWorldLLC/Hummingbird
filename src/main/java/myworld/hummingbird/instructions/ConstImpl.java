package myworld.hummingbird.instructions;

import myworld.hummingbird.Opcode;

public class ConstImpl extends InstructionImpl {

    protected final long value;

    public ConstImpl(Opcode ins, int ip, InstructionImpl next, long value) {
        super(ins, ip, next);
        this.value = value;
    }

    @Override
    public int apply(long[] reg, int regOffset, int ip, InstructionImpl[] instructions) {
        reg[regOffset + ins.dst()] = value;
        return instructions[ip + 1].apply(reg, regOffset, ip + 1, instructions);
    }
}
