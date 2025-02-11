package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class SCompImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {

        
        var vm = fiber.vm;

        var a = (int) registers[ins.src()];
        var b = (int) registers[ins.extra()];

        registers[ins.dst()] = vm.objectToString(a).compareTo(vm.objectToString(b));

        return OpcodeImpl.chainNext(instructions, fiber, registers, ip);
    }
}
