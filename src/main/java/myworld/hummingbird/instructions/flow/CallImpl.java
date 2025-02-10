package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class CallImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        return OpcodeImpl.dispatchCall(fiber, ins, registers, regOffset, ip, ins.src());
    }
}
