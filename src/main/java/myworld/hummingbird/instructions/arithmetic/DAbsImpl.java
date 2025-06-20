package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class DAbsImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        /*var reg = fiber.registers;
        reg[regOffset + ins.dst()] = Double.doubleToLongBits(
                Math.abs(
                        Double.longBitsToDouble(reg[regOffset + ins.src()])
                )
        );*/
        return ip + 1;
    }
}
