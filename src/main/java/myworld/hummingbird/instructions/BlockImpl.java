package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class BlockImpl implements OpcodeImpl {

    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        fiber.setState(Fiber.State.BLOCKED);
        fiber.saveCallContext(ip, regOffset, 0);
        return Integer.MAX_VALUE;
    }

}
