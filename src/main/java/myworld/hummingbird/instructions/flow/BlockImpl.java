package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class BlockImpl implements OpcodeImpl {

    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        fiber.setState(Fiber.State.BLOCKED);
        fiber.saveCallContext(ip,  /*TODO*/ 0);
        return Integer.MAX_VALUE;
    }

}
