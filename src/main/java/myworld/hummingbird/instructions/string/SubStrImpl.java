package myworld.hummingbird.instructions.string;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class SubStrImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int regOffset, int ip) {

        
        var vm = fiber.vm;

        var dst = (int) registers[ins.dst()];
        var src = (int) registers[ins.src()];

        var start = (int) registers[ins.extra()];
        var end = (int) registers[ins.extra1()];

        var str = vm.objectToString(src);

        vm.objMemory[dst] = str.substring(Math.max(0, start), Math.min(str.length(), end));

        return OpcodeImpl.chainNext(instructions, fiber, registers, regOffset, ip);
    }
}
