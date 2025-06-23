package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public interface OpcodeImpl {

    int apply(Fiber fiber, Opcode ins, int ip);

    static int dispatchCall(Fiber fiber, Opcode ins, int ip, int target){

        var callerOffset = fiber.registerOffset;
        var paramOffset = ins.extra();

        fiber.saveCallContext(ip + 1, ins.dst());

        for(int i = 0; i < ins.extra1(); i++){
            fiber.register(i, fiber.rawRegister(callerOffset + paramOffset + i));
        }

        ip = target;
        return ip;
    }

    static int foreignCall(Fiber fiber, Opcode ins, int ip, int symbolIndex){
        var symbol = fiber.exe.symbols()[symbolIndex];
        var func = fiber.vm.foreign[symbol.offset()];

        fiber.saveCallContext(ip + 1, ins.dst());

        ip = ip + 1;
        try {
            func.call(fiber.vm, fiber);
            fiber.restoreCallContext();
        } catch (Exception e) {
            ip = fiber.vm.trap(e, fiber, ip);
        }
        return ip;
    }
}
