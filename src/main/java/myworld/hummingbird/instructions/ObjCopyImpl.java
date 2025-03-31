package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class ObjCopyImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;

        var dst = (int) reg[regOffset + ins.dst()];
        var start = (int) reg[regOffset + ins.src()];
        var end = (int) reg[regOffset + ins.extra()];

        fiber.vm.copyObj(dst, start, end - start);

        return ip + 1;
    }
}
