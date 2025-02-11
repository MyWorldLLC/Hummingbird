package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class ObjCopyImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {
        
        var objMemory = fiber.vm.objMemory;

        var dst = (int) registers[ins.dst()];
        var start = (int) registers[ins.src()];
        var end = (int) registers[ins.extra()];

        System.arraycopy(objMemory, start, objMemory, dst, end - start);

        return OpcodeImpl.chainNext(instructions, fiber, registers, ip);
    }
}
