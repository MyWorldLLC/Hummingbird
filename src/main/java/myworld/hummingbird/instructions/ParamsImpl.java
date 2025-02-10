package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class ParamsImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        for(int i = 0; i < ins.extra(); i++){
            registers[ins.dst() + i] =
                    registers[ins.src() + i];
        }
        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
