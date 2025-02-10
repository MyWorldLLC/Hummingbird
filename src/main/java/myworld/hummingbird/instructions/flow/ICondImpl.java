package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

import static myworld.hummingbird.Opcodes.*;

public class ICondImpl implements OpcodeImpl {

    private final boolean jump;

    public ICondImpl(){
        this(true);
    }

    public ICondImpl(boolean jumpMode){
        jump = jumpMode;
    }

    public boolean isJumpMode(){
        return jump;
    }

    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        var dst = ins.dst();
        var src = ins.src();
        
        var result = switch (ins.extra()) {
            case COND_LT -> registers[dst] < registers[src];
            case COND_LE -> registers[dst] <= registers[src];
            case COND_EQ -> registers[dst] == registers[src];
            case COND_NE -> registers[dst] != registers[src];
            case COND_GE -> registers[dst] >= registers[src];
            case COND_GT -> registers[dst] > registers[src];
            default -> false;
        };

        if(jump){
            if(result){
                return ins.extra1();
            }
        }else{
            registers[ins.extra1()] = result ? 1 : 0;
        }
        return ip + 1;
    }
}
