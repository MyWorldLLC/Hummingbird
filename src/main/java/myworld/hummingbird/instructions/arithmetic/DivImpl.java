package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.Traps;
import myworld.hummingbird.instructions.OpcodeImpl;

public class DivImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {
        
        try {
            registers[ins.dst()] = registers[ins.src()] / registers[ins.extra()];
        } catch (ArithmeticException ex) {
            fiber.vm.trap(Traps.DIV_BY_ZERO, registers, ip);
        }

        return OpcodeImpl.chainNext(instructions, fiber, registers, ip);
    }
}
