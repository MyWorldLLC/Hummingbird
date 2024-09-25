package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class CharAtImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        var objMemory = fiber.vm.objMemory;

        var src = (int) reg[regOffset + ins.src()];
        if(objMemory[src] instanceof String s){
            reg[regOffset + ins.dst()] = s.charAt((int) reg[regOffset + ins.extra()]);
        }else{
            reg[regOffset + ins.dst()] = 0;
        }

        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}
