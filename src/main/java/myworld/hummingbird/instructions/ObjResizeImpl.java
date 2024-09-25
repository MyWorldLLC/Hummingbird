package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

import java.nio.ByteBuffer;

public class ObjResizeImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        var objMemory = fiber.vm.objMemory;

        var size = Math.min((int) reg[regOffset + ins.dst()], fiber.vm.limits.objects());

        var next = new Object[size];
        System.arraycopy(objMemory, 0, next, 0, Math.min(size, objMemory.length));
        fiber.vm.objMemory = next;

        reg[regOffset + ins.dst()] = size;

        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}
