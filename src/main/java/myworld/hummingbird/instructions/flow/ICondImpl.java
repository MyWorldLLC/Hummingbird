package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

import static myworld.hummingbird.Opcodes.*;

public class ICondImpl implements OpcodeImpl {

    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var dst = regOffset + ins.dst();
        var src = regOffset + ins.src();
        var reg = fiber.registers;
        var result = switch (ins.extra()) {
            case COND_LT -> reg[dst] < reg[src];
            case COND_LE -> reg[dst] <= reg[src];
            case COND_EQ -> reg[dst] == reg[src];
            case COND_GE -> reg[dst] >= reg[src];
            case COND_GT -> reg[dst] > reg[src];
            default -> false;
        };
        if(result){
            return ins.extra1();
        }
        return ip + 1;
    }
}
