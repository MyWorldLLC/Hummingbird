package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class MulImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] * reg[regOffset + ins.extra()];
        return OpcodeImpl.chainNext2(fiber, regOffset, ip, instructions);
    }
}
