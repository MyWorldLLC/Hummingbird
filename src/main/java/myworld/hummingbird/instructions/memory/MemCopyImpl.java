package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class MemCopyImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        var memory = fiber.vm.memory;

        var dst = (int) reg[regOffset + ins.dst()];
        var start = (int) reg[regOffset + ins.src()];
        var end = (int) reg[regOffset + ins.extra()];

        memory.put(dst, memory.slice(start, end), 0, end - start);

        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}
