package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class LCopyImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        fiber.vm.copy(fiber.regPointer(ins.dst()), fiber.regPointer(ins.src()), 2);
        return ip + 1;
    }
}
