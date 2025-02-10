package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class AllocatedImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        registers[ins.dst()] = fiber.vm.memory.capacity();
        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
