package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class DebugImpl implements OpcodeImpl {

    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        var debugHandler = fiber.vm.getDebugHandler();
        if(debugHandler != null){
            debugHandler.debug(fiber, ins.dst(), registers[ins.src()]);
        }
        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
