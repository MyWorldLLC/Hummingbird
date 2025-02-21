package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

import static myworld.hummingbird.Fiber.CALL_FRAME_SAVED_REGISTERS;

public class ReturnImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {

        fiber.restoreCallContext();

        var reg = fiber.registers;
        reg[regOffset - CALL_FRAME_SAVED_REGISTERS] = reg[regOffset + ins.dst()];

        return -fiber.ip;
    }
}
