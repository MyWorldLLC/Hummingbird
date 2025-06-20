package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class SubImpl implements OpcodeImpl {

    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        /*var reg = fiber.registers;
        reg[offset + ins.dst()] = reg[offset + ins.src()] - reg[offset + ins.extra()];*/
        return ip + 1;
    }

}
