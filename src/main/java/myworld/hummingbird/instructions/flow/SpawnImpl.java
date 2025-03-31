package myworld.hummingbird.instructions.flow;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class SpawnImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var vm = fiber.vm;
        vm.writeObj((int) fiber.registers[regOffset + ins.dst()], vm.spawn(ins.src(), fiber.registers));
        return ip + 1;
    }
}
