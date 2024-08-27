package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class YieldImpl implements OpcodeImpl {

    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        fiber.saveCallContext(ip + 1, regOffset, 0);
        return Integer.MAX_VALUE;
    }

}
