package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class AbsImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        /*var reg = fiber.registers;
        reg[regOffset + ins.dst()] = Math.abs(reg[regOffset + ins.src()]);*/
        return ip + 1;
    }
}
