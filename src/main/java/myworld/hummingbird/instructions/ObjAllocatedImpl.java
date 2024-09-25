package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class ObjAllocatedImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        fiber.registers[regOffset + ins.dst()] = fiber.vm.objMemory.length;
        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}
