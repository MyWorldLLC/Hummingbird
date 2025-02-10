package myworld.hummingbird.instructions.bitwise;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class BnotImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        
        registers[ins.dst()] = ~registers[ins.src()];
        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
