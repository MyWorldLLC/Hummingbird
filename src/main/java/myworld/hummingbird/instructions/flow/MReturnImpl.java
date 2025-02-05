package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class MReturnImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {

        fiber.restoreCallContext();

        var result = fiber.returnDest;
        var returnOffset = fiber.registerOffset;
        var reg = fiber.registers;

        System.arraycopy(reg, regOffset + ins.dst(), reg, returnOffset + result, ins.src());

        return fiber.ip;
    }
}
