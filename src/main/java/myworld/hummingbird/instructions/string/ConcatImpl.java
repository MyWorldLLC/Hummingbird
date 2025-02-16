package myworld.hummingbird.instructions.string;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class ConcatImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {

        var reg = fiber.registers;
        var vm = fiber.vm;

        var dst = (int) reg[regOffset + ins.dst()];
        var a = (int) reg[regOffset + ins.src()];
        var b = (int) reg[regOffset + ins.extra()];

        vm.objMemory[dst] = vm.objectToString(a) + vm.objectToString(b);

        return ip + 1;
    }
}
