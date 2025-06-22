package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class DSubImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        fiber.longRegister(ins.dst(), Double.doubleToLongBits(
                Double.longBitsToDouble(fiber.longRegister(ins.src()))
                        - Double.longBitsToDouble(fiber.longRegister(ins.extra()))));
        return ip + 1;
    }
}
