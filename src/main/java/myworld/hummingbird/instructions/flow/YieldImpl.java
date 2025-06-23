package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class YieldImpl implements OpcodeImpl {

    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        fiber.saveCallContext(ip + 1, Fiber.YIELDED_RDEST);
        fiber.vm.enqueue(fiber);
        return -Integer.MAX_VALUE;
    }

}
