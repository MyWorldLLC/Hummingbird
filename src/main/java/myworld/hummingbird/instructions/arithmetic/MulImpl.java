package myworld.hummingbird.instructions.arithmetic;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.HummingbirdException;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class MulImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var reg = fiber.registers;
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] * reg[regOffset + ins.extra()];
        return ip + 1; //chainNext(fiber, regOffset, ip, instructions);
    }

    private int chainNext(Fiber fiber, int regOffset, int ip, Opcode[] instructions){
        try{
            var next = instructions[ip + 1];
            return next.impl().apply(fiber, next, regOffset, ip + 1, instructions);
        }catch (HummingbirdException e){
            throw e;
        }catch (Throwable t){
            return fiber.vm.trap(t, fiber.registers, ip);
        }
    }
}
