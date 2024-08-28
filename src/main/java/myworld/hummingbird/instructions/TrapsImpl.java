package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class TrapsImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        fiber.trapHandlerCount = ins.src();
        fiber.trapTableAddr = ins.dst();
        return OpcodeImpl.chainNext(fiber, regOffset, ip + 1, instructions);
    }
}
