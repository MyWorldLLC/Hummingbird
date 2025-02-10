package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class AddImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int offset, int ip) {
        
        registers[ins.dst()] = registers[ins.src()] + registers[ins.extra()];
        return OpcodeImpl.chainNext(instructions, fiber, registers, offset, ip);
    }
}
