package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class MReturnImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {

        fiber.restoreCallContext();

        var result = fiber.returnDest;
        var returnOffset = fiber.registerOffset;
        
        // TODO
        System.arraycopy(registers, ins.dst(), registers, returnOffset + result, ins.src());

        return fiber.ip;
    }
}
