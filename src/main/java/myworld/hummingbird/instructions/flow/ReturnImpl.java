package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class ReturnImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {

        fiber.restoreCallContext();
        // TODO

        var result = fiber.returnDest;
        var returnOffset = fiber.registerOffset;
        

        registers[returnOffset + result] = registers[ins.dst()];

        return fiber.ip;
    }
}
