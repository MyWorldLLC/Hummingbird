package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class StWriteImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        var dst = fiber.register(ins.dst());
        for(int i = 0; i < ins.extra(); i++){
            fiber.register(dst + i, fiber.register(ins.src() + i));
        }
        return ip + 1;
    }
}
