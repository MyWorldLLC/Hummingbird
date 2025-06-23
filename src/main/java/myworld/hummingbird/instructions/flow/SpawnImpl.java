package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class SpawnImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        var vm = fiber.vm;
        vm.writeObj(fiber.register(ins.dst()), vm.spawn(ins.src(), fiber.register(ins.extra()), fiber.register(ins.extra1())));
        return ip + 1;
    }
}
