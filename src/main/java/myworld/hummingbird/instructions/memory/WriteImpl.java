package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class WriteImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        var vm = fiber.vm;
        var dst = fiber.register(ins.dst()) + ins.extra1();
        switch (ins.extra()){
            case 1 -> vm.writeByte(dst, (byte) fiber.register(ins.src()));
            case 2 -> vm.writeShort(dst, (short) fiber.register(ins.src()));
            case 4 -> vm.writeInt(dst, fiber.register(ins.src()));
            case 8 -> vm.writeLong(dst, fiber.longRegister(ins.src()));
            default -> throw new IllegalArgumentException("Memory access must be 1,2,4, or 8: " + ins.extra());
        }
        return ip + 1;
    }
}
