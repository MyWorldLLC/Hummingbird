package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class UnblockImpl implements OpcodeImpl {

    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        var vm = fiber.vm;
        vm.unblock((Fiber) vm.readObj(fiber.register(ins.dst())));
        return ip + 1;
    }

}
