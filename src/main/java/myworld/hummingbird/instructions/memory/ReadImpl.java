package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class ReadImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {
        
        var memory = fiber.vm.memory;

        var src = registers[ins.src()] + ins.extra1();
        registers[ins.dst()] = switch (ins.extra()) {
            case 1 -> memory.get(src);
            case 2 -> memory.getShort(src);
            case 4 -> memory.getInt(src);
            case 8 -> (int) memory.getLong(src); // TODO
            default -> throw new IllegalArgumentException("Memory access must be 1,2,4, or 8: " + ins.extra());
        };
        return OpcodeImpl.chainNext(instructions, fiber, registers, ip);
    }
}
