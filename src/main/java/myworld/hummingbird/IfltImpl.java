package myworld.hummingbird;

import static myworld.hummingbird.Opcodes.*;

public class IfltImpl implements OpcodeImpl {

    @Override
    public int apply(Opcode ins, long[] reg, int regOffset, int ip) {
        var dst = regOffset + ins.dst();
        var src = regOffset + ins.src();
        var result = switch (ins.extra()) {
            case COND_LT -> reg[dst] < reg[src];
            case COND_LE -> reg[dst] <= reg[src];
            case COND_EQ -> reg[dst] == reg[src];
            case COND_GE -> reg[dst] >= reg[src];
            case COND_GT -> reg[dst] > reg[src];
            default -> false;
        };
        if(result){
            ip = ins.extra1();
        }
        return ip;
    }
}
