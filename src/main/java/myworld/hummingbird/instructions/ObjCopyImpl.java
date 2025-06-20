package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class ObjCopyImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {

        var dst = fiber.register(ins.dst());
        var start = fiber.register(ins.src());
        var end = fiber.register(ins.extra());

        fiber.vm.copyObj(dst, start, end - start);

        return ip + 1;
    }
}
