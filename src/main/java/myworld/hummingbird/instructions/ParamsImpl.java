package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class ParamsImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        for(int i = 0; i < ins.extra(); i++){
            fiber.register(ins.dst() + i,
                    fiber.register(fiber.callerRegisterOffset() + ins.src() + i));
        }
        return ip + 1;
    }
}
