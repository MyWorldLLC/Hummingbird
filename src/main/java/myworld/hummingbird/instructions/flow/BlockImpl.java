package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class BlockImpl implements OpcodeImpl {

    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        fiber.setState(Fiber.State.BLOCKED);
        fiber.saveCallContext(ip + 1, Fiber.YIELDED_RDEST);
        return Integer.MAX_VALUE;
    }

}
