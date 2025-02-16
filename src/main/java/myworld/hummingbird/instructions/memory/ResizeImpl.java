package myworld.hummingbird.instructions.memory;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

import java.nio.ByteBuffer;

public class ResizeImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        var memory = fiber.vm.memory;

        var size = Math.min((int) reg[regOffset + ins.src()], fiber.vm.limits.bytes());

        var next = ByteBuffer.allocate(size);
        next.put(0, memory, 0, Math.min(size, memory.capacity()));
        fiber.vm.memory = next;

        reg[regOffset + ins.dst()] = size;

        return ip + 1;
    }
}
