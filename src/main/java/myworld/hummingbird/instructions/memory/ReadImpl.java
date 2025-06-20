package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class ReadImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        /*var reg = fiber.registers;
        var vm = fiber.vm;

        var src = (int) reg[regOffset + ins.src()] + ins.extra1();
        reg[regOffset + ins.dst()] = switch (ins.extra()) {
            case 1 -> vm.readByte(src);
            case 2 -> vm.readShort(src);
            case 4 -> vm.readInt(src);
            case 8 -> vm.readLong(src);
            default -> throw new IllegalArgumentException("Memory access must be 1,2,4, or 8: " + ins.extra());
        };*/
        return ip + 1;
    }
}
