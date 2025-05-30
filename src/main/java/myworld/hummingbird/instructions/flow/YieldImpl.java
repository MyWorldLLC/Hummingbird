package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class YieldImpl implements OpcodeImpl {

    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        fiber.saveCallContext(ip + 1, regOffset, 0);
        fiber.vm.enqueue(fiber);
        return -Integer.MAX_VALUE;
    }

}
