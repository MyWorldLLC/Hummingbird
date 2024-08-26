package myworld.hummingbird.instructions;

import myworld.hummingbird.Opcode;

import static myworld.hummingbird.Opcodes.*;

public class ICondImpl extends InstructionImpl {

    public ICondImpl(Opcode ins, int ip, InstructionImpl next) {
        super(ins, ip, next);
    }

    @Override
    public int apply(long[] reg, int regOffset, int ip, InstructionImpl[] instructions) {
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
            return ins.extra1();
        }
        return ip + 1;
    }
}
