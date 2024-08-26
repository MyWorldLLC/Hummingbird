package myworld.hummingbird.instructions;

import myworld.hummingbird.Opcode;

public class AddImpl extends InstructionImpl {

    protected final int dst;
    protected final int src;
    protected final int extra;

    public AddImpl(Opcode ins, int ip, InstructionImpl next) {
        super(ins, ip, next);
        dst = ins.dst();
        src = ins.src();
        extra = ins.extra();
    }

    @Override
    public int apply(long[] registers, int offset) {
        registers[offset + dst] = registers[offset + src] + registers[offset + extra];
        return next.apply(registers, offset);
    }
}