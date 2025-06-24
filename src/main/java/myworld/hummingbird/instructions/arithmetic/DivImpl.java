package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.Traps;
import myworld.hummingbird.instructions.OpcodeImpl;

public class DivImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip) {
        try {
            fiber.register(ins.dst(), fiber.register(ins.src()) / fiber.register(ins.extra()));
        }catch (ArithmeticException ex){
            return fiber.vm.trap(Traps.DIV_BY_ZERO, fiber, ip);
        }
        return ip + 1;
    }
}
