package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class MulImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        
        registers[ins.dst()] = registers[ins.src()] * registers[ins.extra()];
        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
