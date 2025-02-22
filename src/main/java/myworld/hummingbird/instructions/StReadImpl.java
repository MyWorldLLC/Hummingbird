package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class StReadImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        var src = (int) reg[regOffset + ins.src()];
        for(int i = 0; i < ins.extra(); i++){
            reg[regOffset + ins.dst() + i] = reg[src + i];
        }
        return ip + 1;
    }
}
