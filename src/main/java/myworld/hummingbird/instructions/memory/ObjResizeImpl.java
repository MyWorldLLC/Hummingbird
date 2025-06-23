package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class ObjResizeImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        fiber.register(ins.dst(), fiber.vm.resizeObj(fiber.register(ins.src())));
        return ip + 1;
    }
}
