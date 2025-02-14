package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class DDivImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int offset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;

        reg[offset + ins.dst()] = Double.doubleToLongBits(
                Double.longBitsToDouble(reg[offset + ins.src()])
                        / Double.longBitsToDouble(reg[offset + ins.extra()])
        );

        return OpcodeImpl.chainNext2(fiber, offset, ip, instructions);
    }
}
