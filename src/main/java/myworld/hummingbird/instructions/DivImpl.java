package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.Traps;

public class DivImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        try {
            reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] / reg[regOffset + ins.extra()];
        } catch (ArithmeticException ex) {
            fiber.vm.trap(Traps.DIV_BY_ZERO, reg, ip);
        }

        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}
