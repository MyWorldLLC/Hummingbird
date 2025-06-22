package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class LConstImpl implements OpcodeImpl {

    protected final long value;

    public LConstImpl(long value){
        this.value = value;
    }

    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        fiber.longRegister(ins.dst(), value);
        return ip + 1;
    }
}
