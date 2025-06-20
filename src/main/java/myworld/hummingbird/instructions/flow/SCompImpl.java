package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class SCompImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {

        var vm = fiber.vm;

        var a = fiber.register(ins.src());
        var b = fiber.register(ins.extra());

        fiber.register(ins.dst(), vm.objectToString(a).compareTo(vm.objectToString(b)));

        return ip + 1;
    }
}
