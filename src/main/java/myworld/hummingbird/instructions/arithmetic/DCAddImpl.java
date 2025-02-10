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
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {

        // TODO
        registers[ins.dst()] = (int) Double.doubleToLongBits(Double.longBitsToDouble(registers[ins.src()]) + value);
        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
