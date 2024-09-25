package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class DMulImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int offset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;

        reg[offset + ins.dst()] = Double.doubleToLongBits(
                Double.longBitsToDouble(reg[offset + ins.src()])
                        * Double.longBitsToDouble(reg[offset + ins.extra()])
        );

        return OpcodeImpl.chainNext(fiber, offset, ip, instructions);
    }
}
