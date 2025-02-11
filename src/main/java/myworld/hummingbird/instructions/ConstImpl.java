package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class ConstImpl implements OpcodeImpl {

    protected final long value;

    public ConstImpl(long value){
        this.value = value;
    }

    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {
        registers[ins.dst()] = (int) value; // TODO
        return OpcodeImpl.chainNext(instructions, fiber, registers, ip);
    }
}
