package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class TrapImpl implements OpcodeImpl {

    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var registers = fiber.registers;
        return fiber.vm.trap((int)registers[regOffset + ins.dst()], registers, ip);
    }

}
