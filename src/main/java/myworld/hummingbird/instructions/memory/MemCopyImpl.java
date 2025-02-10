package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class MemCopyImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        
        var memory = fiber.vm.memory;

        var dst = (int) registers[ins.dst()];
        var start = (int) registers[ins.src()];
        var end = (int) registers[ins.extra()];

        memory.put(dst, memory.slice(start, end), 0, end - start);

        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
