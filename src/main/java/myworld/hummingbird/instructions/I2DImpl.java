package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class I2DImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        fiber.longRegister(ins.dst(), Double.doubleToLongBits((double) fiber.register(ins.src())));
        return ip + 1;
    }
}
