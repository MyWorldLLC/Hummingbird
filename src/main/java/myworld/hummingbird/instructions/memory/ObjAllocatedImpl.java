package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class ObjAllocatedImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        registers[ins.dst()] = fiber.vm.objMemory.length;
        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
