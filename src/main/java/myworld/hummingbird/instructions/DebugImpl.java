package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class DebugImpl implements OpcodeImpl {

    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var debugHandler = fiber.vm.getDebugHandler();
        if(debugHandler != null){
            debugHandler.debug(fiber, ins.dst(), fiber.registers[regOffset + ins.src()]);
        }
        return ip + 1;
    }
}
