package myworld.hummingbird.instructions;

import myworld.hummingbird.Opcode;

public class ReturnImpl extends InstructionImpl {

    public ReturnImpl(Opcode ins, int ip, InstructionImpl next) {
        super(ins, ip, next);
    }

    @Override
    public int apply(long[] reg, int regOffset) {
        return next.ip; // TODO
    }
}