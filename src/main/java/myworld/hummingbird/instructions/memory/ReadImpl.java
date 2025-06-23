package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class ReadImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        var vm = fiber.vm;
        var src = fiber.register(ins.src()) + ins.extra1();
        switch (ins.extra()){
            case 1 -> fiber.register(ins.dst(), vm.readByte(src));
            case 2 -> fiber.register(ins.dst(), vm.readShort(src));
            case 4 -> fiber.register(ins.dst(), vm.readInt(src));
            case 8 -> fiber.longRegister(ins.dst(), vm.readLong(src));
            default -> throw new IllegalArgumentException("Memory access must be 1,2,4, or 8: " + ins.extra());
        }
        return ip + 1;
    }
}
