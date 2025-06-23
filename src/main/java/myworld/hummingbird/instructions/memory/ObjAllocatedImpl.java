package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class ObjAllocatedImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        fiber.register(ins.dst(), fiber.vm.objMemorySize());
        return ip + 1;
    }
}
