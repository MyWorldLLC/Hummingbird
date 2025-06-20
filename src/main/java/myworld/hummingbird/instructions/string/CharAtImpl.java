package myworld.hummingbird.instructions.string;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class CharAtImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        /*var reg = fiber.registers;

        var src = (int) reg[regOffset + ins.src()];
        if(fiber.vm.readObj(src) instanceof String s){
            reg[regOffset + ins.dst()] = s.charAt((int) reg[regOffset + ins.extra()]);
        }else{
            reg[regOffset + ins.dst()] = 0;
        }*/

        return ip + 1;
    }
}
