package myworld.hummingbird.instructions;

import myworld.hummingbird.Opcode;

public class TerminalImpl extends InstructionImpl {
    public TerminalImpl(Opcode ins, int ip, InstructionImpl next) {
        super(ins, ip, next);
    }

    @Override
    public int apply(long[] reg, int regOffset) {
        return ip;
    }
}
