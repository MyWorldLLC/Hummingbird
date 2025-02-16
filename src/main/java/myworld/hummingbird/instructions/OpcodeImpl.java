package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.HummingbirdException;
import myworld.hummingbird.Opcode;

public interface OpcodeImpl {
    int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions);

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
