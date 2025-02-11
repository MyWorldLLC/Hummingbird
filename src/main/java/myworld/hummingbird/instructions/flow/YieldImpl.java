package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class YieldImpl implements OpcodeImpl {

    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {
        fiber.saveCallContext(ip + 1, 0, 0);
        fiber.vm.enqueue(fiber);
        return Integer.MAX_VALUE;
    }

}
