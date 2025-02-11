package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class BlockImpl implements OpcodeImpl {

    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {
        fiber.setState(Fiber.State.BLOCKED);
        fiber.saveCallContext(ip, 0, 0);
        return Integer.MAX_VALUE;
    }

}
