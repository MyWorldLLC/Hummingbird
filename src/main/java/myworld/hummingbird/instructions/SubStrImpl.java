package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class SubStrImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {

        var reg = fiber.registers;
        var vm = fiber.vm;

        var dst = (int) reg[regOffset + ins.dst()];
        var src = (int) reg[regOffset + ins.src()];

        var start = (int) reg[regOffset + ins.extra()];
        var end = (int) reg[regOffset + ins.extra1()];

        var str = vm.objectToString(src);

        vm.objMemory[dst] = str.substring(Math.max(0, start), Math.min(str.length(), end));

        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}
