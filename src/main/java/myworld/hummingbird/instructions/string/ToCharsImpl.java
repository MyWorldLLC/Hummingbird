package myworld.hummingbird.instructions.string;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class ToCharsImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {
        
        var memory = fiber.vm.memory;
        var charBuf = memory.asCharBuffer();

        var dst = (int) registers[ins.dst()];
        var src = (int) registers[ins.src()];

        if (fiber.vm.objMemory[src] instanceof String s) {
            var chars = s.toCharArray();
            memory.putInt(dst, chars.length);
            charBuf.put((dst + 4) / 2, chars);
        }else{
            memory.putInt(dst, -1);
        }

        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
