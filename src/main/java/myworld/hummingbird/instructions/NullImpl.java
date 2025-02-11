package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class NullImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {
        fiber.vm.objMemory[ins.dst()] = null;
        return OpcodeImpl.chainNext(instructions, fiber, registers, ip);
    }
}
