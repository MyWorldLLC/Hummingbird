package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class WriteImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {
        
        var memory = fiber.vm.memory;

        var dst = (int) registers[ins.dst()] + ins.extra1();
        var src = registers[ins.src()];
        switch (ins.extra()) {
            case 1 -> memory.put(dst, (byte) src);
            case 2 -> memory.putShort(dst, (short) src);
            case 4 -> memory.putInt(dst, (int) src);
            case 8 -> memory.putLong(dst, src);
            default -> throw new IllegalArgumentException("Memory access must be 1,2,4, or 8: " + ins.extra());
        }
        return OpcodeImpl.chainNext(instructions, fiber, registers, ip);
    }
}
