package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class UnblockImpl implements OpcodeImpl {

    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var vm = fiber.vm;
        ((Fiber) vm.objMemory[(int) fiber.registers[regOffset + ins.dst()]]).setState(Fiber.State.RUNNABLE);
         return ip + 1;
    }

}
