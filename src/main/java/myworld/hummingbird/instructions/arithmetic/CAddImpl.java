package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class CAddImpl implements OpcodeImpl {

    private final long value;

    public CAddImpl(long value){
        this.value = value;
    }

    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        /*var reg = fiber.registers;
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] + value;*/
        return ip + 1;
    }
}
