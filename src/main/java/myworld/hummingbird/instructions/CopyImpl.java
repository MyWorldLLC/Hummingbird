package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class CopyImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {
        registers[ins.dst()] = registers[ins.src()];
        return OpcodeImpl.chainNext(instructions, fiber, registers, ip);
    }
}
