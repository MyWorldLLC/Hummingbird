package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class ToCharsImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        var memory = fiber.vm.memory;
        var charBuf = memory.asCharBuffer();

        var dst = (int) reg[regOffset + ins.dst()];
        var src = (int) reg[regOffset + ins.src()];

        if (fiber.vm.objMemory[src] instanceof String s) {
            var chars = s.toCharArray();
            memory.putInt(dst, chars.length);
            charBuf.put((dst + 4) / 2, chars);
        }else{
            memory.putInt(dst, -1);
        }

        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}
