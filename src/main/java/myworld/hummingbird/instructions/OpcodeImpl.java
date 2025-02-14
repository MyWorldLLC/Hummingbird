package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.HummingbirdException;
import myworld.hummingbird.Opcode;

/**
 * OpcodeImpl defines several identical chainNext() methods. These are intended to be
 * used by opcodes such that they help the CPU branch predictor correctly predict the next instruction
 * that will run. The technical motivation and explanation for this pattern is available at:
 * <a href="http://www.emulators.com/docs/nx25_nostradamus.htm">...</a>
 * <p>
 * In some benchmarks this will not have much (if any) impact, but in others the speedup is significant. The highest
 * measured speedup was ~20%.
 */
public interface OpcodeImpl {
    int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions);

    static int chainNext(Fiber fiber, int regOffset, int ip, Opcode[] instructions){
        try{
            var next = instructions[ip + 1];
            return next.impl().apply(fiber, next, regOffset, ip + 1, instructions);
        }catch (HummingbirdException e){
            throw e;
        }catch (Throwable t){
            return fiber.vm.trap(t, fiber.registers, ip);
        }
    }

    static int chainNext2(Fiber fiber, int regOffset, int ip, Opcode[] instructions){
        try{
            var next = instructions[ip + 1];
            return next.impl().apply(fiber, next, regOffset, ip + 1, instructions);
        }catch (HummingbirdException e){
            throw e;
        }catch (Throwable t){
            return fiber.vm.trap(t, fiber.registers, ip);
        }
    }

    static int dispatchCall(Fiber fiber, Opcode ins, int regOffset, int ip, int symbolIndex){
        var symbol = fiber.exe.symbols()[symbolIndex];

        var callerOffset = regOffset;
        var callerParams = ins.extra();
        fiber.saveCallContext(ip + 1, regOffset, ins.dst());

        var reg = fiber.registers;

        regOffset += symbol.registers();
        for(int i = 0; i < ins.extra1(); i++){
            reg[regOffset + i] = reg[callerOffset + callerParams + i];
        }

        fiber.registerOffset = regOffset;
        ip = symbol.offset();
        fiber.ip = ip;
        return ip;
    }

    static int foreignCall(Fiber fiber, Opcode ins, int regOffset, int ip, int symbolIndex){
        var symbol = fiber.exe.symbols()[symbolIndex];
        var func = fiber.vm.foreign[symbol.offset()];

        fiber.saveCallContext(ip + 1, regOffset, ins.dst());

        ip = ip + 1;
        try {
            func.call(fiber.vm, fiber);
        } catch (Exception e) {
            ip = fiber.vm.trap(e, fiber.registers, ip);
        }
        return ip;
    }
}
