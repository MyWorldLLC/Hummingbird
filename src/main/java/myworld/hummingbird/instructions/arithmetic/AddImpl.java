package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class AddImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int offset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        reg[offset + ins.dst()] = reg[offset + ins.src()] + reg[offset + ins.extra()];
        return OpcodeImpl.chainNext(fiber, offset, ip, instructions);
    }
}
