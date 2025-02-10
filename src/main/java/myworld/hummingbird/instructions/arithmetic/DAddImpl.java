package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class DAddImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int offset, int ip) {
        
        // TODO
        registers[ins.dst()] = (int) Double.doubleToLongBits(
                Double.longBitsToDouble(registers[ins.src()])
                        + Double.longBitsToDouble(registers[ins.extra()])
        );

        return OpcodeImpl.chainNext(instructions, fiber, registers, offset, ip);
    }
}
