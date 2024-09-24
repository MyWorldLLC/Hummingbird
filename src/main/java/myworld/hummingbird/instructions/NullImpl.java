package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class NullImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        fiber.vm.objMemory[regOffset + ins.dst()] = null;
        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}