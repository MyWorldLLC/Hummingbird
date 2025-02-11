package myworld.hummingbird.instructions.string;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class CharAtImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {
        
        var objMemory = fiber.vm.objMemory;

        var src = (int) registers[ins.src()];
        if(objMemory[src] instanceof String s){
            registers[ins.dst()] = s.charAt((int) registers[ins.extra()]);
        }else{
            registers[ins.dst()] = 0;
        }

        return OpcodeImpl.chainNext(instructions, fiber, registers, ip);
    }
}
