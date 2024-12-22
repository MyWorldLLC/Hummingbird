package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

import static myworld.hummingbird.Opcodes.*;

public class DCondImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var dst = regOffset + ins.dst();
        var src = regOffset + ins.src();
        var reg = fiber.registers;
        var result = switch (ins.extra()) {
            case COND_LT -> Double.longBitsToDouble(reg[dst]) < Double.longBitsToDouble(reg[src]);
            case COND_LE -> Double.longBitsToDouble(reg[dst]) <= Double.longBitsToDouble(reg[src]);
            case COND_EQ -> Double.longBitsToDouble(reg[dst]) == Double.longBitsToDouble(reg[src]);
            case COND_NE -> Double.longBitsToDouble(reg[dst]) != Double.longBitsToDouble(reg[src]);
            case COND_GE -> Double.longBitsToDouble(reg[dst]) >= Double.longBitsToDouble(reg[src]);
            case COND_GT -> Double.longBitsToDouble(reg[dst]) > Double.longBitsToDouble(reg[src]);
            default -> false;
        };
        if(result){
            return ins.extra1();
        }
        return ip + 1;
    }
}
