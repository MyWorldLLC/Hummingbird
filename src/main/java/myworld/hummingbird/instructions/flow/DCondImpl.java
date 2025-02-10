package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

import static myworld.hummingbird.Opcodes.*;

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
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        var dst = ins.dst();
        var src = ins.src();
        
        var result = switch (ins.extra()) {
            case COND_LT -> Double.longBitsToDouble(registers[dst]) < Double.longBitsToDouble(registers[src]);
            case COND_LE -> Double.longBitsToDouble(registers[dst]) <= Double.longBitsToDouble(registers[src]);
            case COND_EQ -> Double.longBitsToDouble(registers[dst]) == Double.longBitsToDouble(registers[src]);
            case COND_NE -> Double.longBitsToDouble(registers[dst]) != Double.longBitsToDouble(registers[src]);
            case COND_GE -> Double.longBitsToDouble(registers[dst]) >= Double.longBitsToDouble(registers[src]);
            case COND_GT -> Double.longBitsToDouble(registers[dst]) > Double.longBitsToDouble(registers[src]);
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
