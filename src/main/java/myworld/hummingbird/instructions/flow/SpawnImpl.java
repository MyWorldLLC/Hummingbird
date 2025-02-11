package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class SpawnImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip) {
        var vm = fiber.vm;
        vm.objMemory[(int) registers[ins.dst()]] = vm.spawn(ins.src(), registers);
        return ip + 1;
    }
}
