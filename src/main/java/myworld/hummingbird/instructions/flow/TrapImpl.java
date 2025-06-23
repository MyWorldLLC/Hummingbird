package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class TrapImpl implements OpcodeImpl {

    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        return fiber.vm.trap(fiber.register(ins.dst()), fiber, ip);
    }

}
