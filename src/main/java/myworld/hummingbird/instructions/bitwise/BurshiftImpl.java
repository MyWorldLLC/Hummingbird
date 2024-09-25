package myworld.hummingbird.instructions.bitwise;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class BurshiftImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] >>> reg[regOffset + ins.extra()];
        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}
