package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class StReadImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        var src = fiber.register(ins.src());
        for(int i = 0; i < ins.extra(); i++){
           fiber.register(ins.dst() + i, fiber.register(src + i));
        }
        return ip + 1;
    }
}
