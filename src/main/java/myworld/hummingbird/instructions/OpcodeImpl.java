package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public interface OpcodeImpl {

    int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions);

    static int dispatchCall(Fiber fiber, Opcode ins, int ip, int target){

        var callerOffset = fiber.registerOffset;
        var paramCount = ins.extra();
        var regOffset = fiber.registerOffset; // TODO
        regOffset = fiber.saveCallContext(ip + 1, ins.dst());

        for(int i = 0; i < ins.extra1(); i++){
            fiber.register(regOffset + i, fiber.register(callerOffset + paramCount + i));
        }

        ip = target;
        return ip;
    }

    static int foreignCall(Fiber fiber, Opcode ins, int ip, int symbolIndex){
        var symbol = fiber.exe.symbols()[symbolIndex];
        var func = fiber.vm.foreign[symbol.offset()];

        fiber.saveCallContext(ip + 1,  /*TODO*/ ins.dst());

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
