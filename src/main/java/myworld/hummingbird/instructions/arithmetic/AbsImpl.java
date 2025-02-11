package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class AbsImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {
        
        registers[ins.dst()] = Math.abs(registers[ins.src()]);
        return OpcodeImpl.chainNext(instructions, fiber, registers, ip);
    }
}
