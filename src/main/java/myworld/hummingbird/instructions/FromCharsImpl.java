package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class FromCharsImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {

        var reg = fiber.registers;
        var vm = fiber.vm;

        var src = (int) reg[regOffset + ins.src()];
        var dst = (int) reg[regOffset + ins.dst()];

        vm.objMemory[dst] = vm.readString(src);

        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}
