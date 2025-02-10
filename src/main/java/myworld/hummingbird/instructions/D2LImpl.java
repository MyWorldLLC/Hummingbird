package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class D2LImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        registers[ins.dst()] = (int) Double.longBitsToDouble(registers[ins.src()]);
        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
