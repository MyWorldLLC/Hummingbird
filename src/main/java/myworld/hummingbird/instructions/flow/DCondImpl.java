package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

import static myworld.hummingbird.Opcodes.*;
import static myworld.hummingbird.Opcodes.COND_GE;
import static myworld.hummingbird.Opcodes.COND_GT;
import static myworld.hummingbird.Opcodes.COND_NE;

public class DCondImpl implements OpcodeImpl {

    private final boolean jump;

    public DCondImpl(){
        this(true);
    }

    public DCondImpl(boolean jumpMode){
        jump = jumpMode;
    }

    public boolean isJumpMode(){
        return jump;
    }

    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {

        var dst = fiber.doubleRegister(ins.dst());
        var src = fiber.doubleRegister(ins.src());
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
