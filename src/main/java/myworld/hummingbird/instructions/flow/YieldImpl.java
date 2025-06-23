package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class YieldImpl implements OpcodeImpl {

    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        fiber.vm.yield(fiber, ip);
        return -Integer.MAX_VALUE;
    }

}
