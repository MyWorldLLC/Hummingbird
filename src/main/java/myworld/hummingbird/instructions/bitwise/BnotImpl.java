package myworld.hummingbird.instructions.bitwise;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class BnotImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        fiber.register(ins.dst(), ~fiber.register(ins.src()));
        return ip + 1;
    }
}
