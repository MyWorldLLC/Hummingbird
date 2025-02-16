package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class ConstImpl implements OpcodeImpl {

    protected final long value;

    public ConstImpl(long value){
        this.value = value;
    }

    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        fiber.registers[regOffset + ins.dst()] = value;
        return ip + 1;
    }
}
