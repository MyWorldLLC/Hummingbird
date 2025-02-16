package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class ParamsImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        for(int i = 0; i < ins.extra(); i++){
            fiber.registers[regOffset + ins.dst() + i] =
                    fiber.registers[fiber.callerRegisterOffset() + ins.src() + i];
        }
        return ip + 1;
    }
}
