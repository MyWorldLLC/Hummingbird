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
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        var dst = fiber.register(ins.dst());
        var src = fiber.register(ins.src());
        var result = switch (ins.extra()) {
            case COND_LT -> dst < src;
            case COND_LE -> dst <= src;
            case COND_EQ -> dst == src;
            case COND_NE -> dst != src;
            case COND_GE -> dst >= src;
            case COND_GT -> dst > src;
            default -> false;
        };

        if(jump){
            if(result){
                return ins.extra1();
            }
        }else{
            fiber.register(ins.extra1(), result ? 1 : 0);
        }
        return ip + 1;
    }
}
