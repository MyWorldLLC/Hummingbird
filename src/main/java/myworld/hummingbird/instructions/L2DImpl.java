package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class L2DImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {

        // TODO
        registers[ins.dst()] = (int) Double.doubleToLongBits((double) registers[ins.src()]);
        return OpcodeImpl.chainNext(instructions, fiber, registers, ip);
    }
}
