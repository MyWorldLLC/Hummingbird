package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class DCallImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        return OpcodeImpl.dispatchCall(fiber, ins, regOffset, ip, (int) fiber.registers[regOffset + ins.src()]);
    }
}
