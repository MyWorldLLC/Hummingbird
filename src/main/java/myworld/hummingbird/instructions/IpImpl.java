package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class IpImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {
        registers[ins.dst()] = ip;
        return OpcodeImpl.chainNext(instructions, fiber, registers, ip);
    }
}
