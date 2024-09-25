package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class SCompImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {

        var reg = fiber.registers;
        var vm = fiber.vm;

        var a = (int) reg[regOffset + ins.src()];
        var b = (int) reg[regOffset + ins.extra()];

        reg[regOffset + ins.dst()] = vm.objectToString(a).compareTo(vm.objectToString(b));

        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}
