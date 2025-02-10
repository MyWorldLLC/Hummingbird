package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class ObjResizeImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        
        var objMemory = fiber.vm.objMemory;

        var size = Math.min((int) registers[ins.src()], fiber.vm.limits.objects());

        var next = new Object[size];
        System.arraycopy(objMemory, 0, next, 0, Math.min(size, objMemory.length));
        fiber.vm.objMemory = next;

        registers[ins.dst()] = size;

        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
