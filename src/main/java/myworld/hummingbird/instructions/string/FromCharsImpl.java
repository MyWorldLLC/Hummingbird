package myworld.hummingbird.instructions.string;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class FromCharsImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {

        
        var vm = fiber.vm;

        var src = (int) registers[ins.src()];
        var dst = (int) registers[ins.dst()];

        vm.objMemory[dst] = vm.readString(src);

        return OpcodeImpl.chainNext(instructions, fiber, registers, ip);
    }
}
