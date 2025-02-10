package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class CAddImpl implements OpcodeImpl {

    private final long value;

    public CAddImpl(long value){
        this.value = value;
    }

    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        // TODO
        registers[ins.dst()] = registers[ins.src()] + (int) value;
        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
