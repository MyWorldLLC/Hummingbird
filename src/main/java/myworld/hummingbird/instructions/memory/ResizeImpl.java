package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

import java.nio.ByteBuffer;

public class ResizeImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        
        var memory = fiber.vm.memory;

        var size = Math.min((int) registers[ins.src()], fiber.vm.limits.bytes());

        var next = ByteBuffer.allocate(size);
        next.put(0, memory, 0, Math.min(size, memory.capacity()));
        fiber.vm.memory = next;

        registers[ins.dst()] = size;

        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
