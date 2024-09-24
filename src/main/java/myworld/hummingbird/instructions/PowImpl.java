package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class PowImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        reg[regOffset + ins.dst()] = (long) Math.pow(reg[regOffset + ins.src()], reg[regOffset + ins.extra()]);
        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}