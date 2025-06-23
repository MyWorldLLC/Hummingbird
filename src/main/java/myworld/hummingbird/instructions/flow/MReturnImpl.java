package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class MReturnImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {

        var vPtr = fiber.regPointer(ins.dst());
        var rDest = fiber.restoreCallContext();

        fiber.vm.copy(rDest, vPtr, ins.src());

        return -fiber.ip;
    }
}
