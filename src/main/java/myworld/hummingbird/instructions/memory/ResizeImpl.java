package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class ResizeImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        /*var reg = fiber.registers;

        reg[regOffset + ins.dst()] = fiber.vm.resize((int) reg[regOffset + ins.src()]);*/

        return ip + 1;
    }
}
