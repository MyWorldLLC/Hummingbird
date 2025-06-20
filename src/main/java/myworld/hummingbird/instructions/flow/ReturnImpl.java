package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

import static myworld.hummingbird.Fiber.CALL_FRAME_SAVED_REGISTERS;

public class ReturnImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        var value = fiber.register(ins.dst());
        var rDest = fiber.restoreCallContext();
        fiber.register(rDest, value);
        return -fiber.ip;
    }
}
