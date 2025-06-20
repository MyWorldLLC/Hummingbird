package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class L2DImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        fiber.longRegister(ins.dst(), Double.doubleToLongBits((double) fiber.longRegister(ins.src())));
        return ip + 1;
    }
}
