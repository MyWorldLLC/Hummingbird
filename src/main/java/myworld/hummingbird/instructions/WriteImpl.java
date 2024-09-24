package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class WriteImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        var memory = fiber.vm.memory;

        var dst = (int) reg[regOffset + ins.dst()] + ins.extra1();
        var src = reg[regOffset + ins.src()];
        switch (ins.extra()) {
            case 1 -> memory.put(dst, (byte) src);
            case 2 -> memory.putShort(dst, (short) src);
            case 4 -> memory.putInt(dst, (int) src);
            case 8 -> memory.putLong(dst, src);
            default -> throw new IllegalArgumentException("Memory access must be 1,2,4, or 8: " + ins.extra());
        }
        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}
