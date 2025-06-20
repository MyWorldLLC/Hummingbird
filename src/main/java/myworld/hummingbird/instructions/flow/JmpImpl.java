package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class JmpImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        return fiber.register(ins.dst());
    }
}
