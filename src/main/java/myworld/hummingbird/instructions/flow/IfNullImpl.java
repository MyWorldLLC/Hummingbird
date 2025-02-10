package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class IfNullImpl implements OpcodeImpl {

    private final boolean jump;

    public IfNullImpl(){
        this(true);
    }

    public IfNullImpl(boolean jumpMode){
        jump = jumpMode;
    }

    public boolean isJumpMode(){
        return jump;
    }

    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        var dst = ins.dst();
        var result = fiber.vm.objMemory[dst] == null;
        if(jump){
            if(result){
                return ins.src();
            }
        }else{
            registers[ins.src()] = result ? 1 : 0;
        }
        return ip + 1;
    }
}
