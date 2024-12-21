package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class DCAddImpl implements OpcodeImpl {

    private final double value;

    public DCAddImpl(double value){
        this.value = value;
    }

    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        reg[regOffset + ins.dst()] = Double.doubleToLongBits(Double.longBitsToDouble(reg[regOffset + ins.src()]) + value);
        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}
