package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class ConstImpl implements OpcodeImpl {

    protected final long value;

    public ConstImpl(long value){
        this.value = value;
    }

    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        fiber.register(ins.dst(), (int) value); // TODO - long support
        return ip + 1;
    }
}
