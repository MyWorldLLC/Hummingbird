package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class TrapsImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        fiber.trapHandlerCount = ins.src();
        fiber.trapTableAddr = ins.dst();
        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip + 1);
    }
}
