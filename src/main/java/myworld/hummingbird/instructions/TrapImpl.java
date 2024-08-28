package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class TrapImpl implements OpcodeImpl {

    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var registers = fiber.registers;
        return fiber.vm.trap((int)registers[regOffset + ins.dst()], registers, ip);
    }

}
