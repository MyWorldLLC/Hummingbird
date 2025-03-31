package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class WriteImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        var vm = fiber.vm;

        var dst = (int) reg[regOffset + ins.dst()] + ins.extra1();
        var src = reg[regOffset + ins.src()];
        switch (ins.extra()) {
            case 1 -> vm.writeByte(dst, (byte) src);
            case 2 -> vm.writeShort(dst, (short) src);
            case 4 -> vm.writeInt(dst, (int) src);
            case 8 -> vm.writeLong(dst, src);
            default -> throw new IllegalArgumentException("Memory access must be 1,2,4, or 8: " + ins.extra());
        }
        return ip + 1;
    }
}
