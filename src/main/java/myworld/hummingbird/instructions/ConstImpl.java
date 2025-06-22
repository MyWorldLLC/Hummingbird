package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class ConstImpl implements OpcodeImpl {

    protected final int value;

    public ConstImpl(int value){
        this.value = value;
    }

    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        fiber.register(ins.dst(), value);
        return ip + 1;
    }
}
