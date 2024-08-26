package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class CopyImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()];
        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}
