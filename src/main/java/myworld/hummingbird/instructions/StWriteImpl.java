package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class StWriteImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        var dst = (int) reg[regOffset + ins.dst()];
        for(int i = 0; i < ins.extra(); i++){
            reg[dst + i] = reg[regOffset + ins.src() + i];
        }
        return ip + 1;
    }
}
