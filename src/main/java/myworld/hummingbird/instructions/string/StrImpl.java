package myworld.hummingbird.instructions.string;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

import java.util.Objects;

public class StrImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        
        var src = registers[ins.src()];

        fiber.vm.objMemory[ins.dst()] = switch (ins.extra()){
            case 0 -> Long.toString(src);
            case 1 -> Double.toString(Double.longBitsToDouble(src));
            case 2 -> Objects.toString(fiber.vm.objMemory[(int) src]);
            default -> null;
        };

        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
