package myworld.hummingbird.instructions.string;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class StrLenImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        
        var objMemory = fiber.vm.objMemory;

        var src = (int) registers[ins.src()];
        if(objMemory[src] instanceof String s){
            registers[ins.dst()] = s.length();
        }else{
            registers[ins.dst()] = -1;
        }

        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
