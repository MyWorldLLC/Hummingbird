package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class MemCopyImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        fiber.vm.copy(fiber.register(ins.dst()), fiber.register(ins.src()), fiber.register(ins.extra()));
        return ip + 1;
    }
}
