package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class DCallImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        return OpcodeImpl.dispatchCall(fiber, ins, regOffset, ip, (int) fiber.registers[regOffset + ins.src()]);
    }
}
