package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class IfNullImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var dst = regOffset + ins.dst();
        if(fiber.vm.objMemory[dst] == null){
            return ins.src();
        }
        return ip + 1;
    }
}
