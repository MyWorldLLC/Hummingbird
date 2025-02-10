package myworld.hummingbird.instructions.string;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class ConcatImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {

        
        var vm = fiber.vm;

        var dst = (int) registers[ins.dst()];
        var a = (int) registers[ins.src()];
        var b = (int) registers[ins.extra()];

        vm.objMemory[dst] = vm.objectToString(a) + vm.objectToString(b);

        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
