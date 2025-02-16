package myworld.hummingbird.instructions.string;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class StrLenImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        var objMemory = fiber.vm.objMemory;

        var src = (int) reg[regOffset + ins.src()];
        if(objMemory[src] instanceof String s){
            reg[regOffset + ins.dst()] = s.length();
        }else{
            reg[regOffset + ins.dst()] = -1;
        }

        return ip + 1;
    }
}
