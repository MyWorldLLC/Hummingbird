package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.Traps;

public interface OpcodeImpl {

    int apply(Fiber fiber, Opcode ins, int ip);

    static int dispatchCall(Fiber fiber, Opcode ins, int ip, int target){

        var symbol = fiber.exe.symbols()[target];

        var callerOffset = fiber.registerOffset;
        var paramStart = ins.extra();
        var paramCount = ins.extra1();

        if(callerOffset + paramStart + paramCount + symbol.registers() + Fiber.CALL_FRAME_SAVED_REGISTERS > fiber.getStackMax()){
            return fiber.vm.trap(Traps.STACK_OVERFLOW, fiber, ip);
        }

        fiber.saveCallContext(ip + 1, ins.dst());

        for(int i = 0; i < ins.extra1(); i++){
            fiber.register(i, fiber.rawRegister(callerOffset + paramStart + i));
        }

        return symbol.offset();
    }

    static int foreignCall(Fiber fiber, Opcode ins, int ip, int symbolIndex){
        var symbol = fiber.exe.symbols()[symbolIndex];
        var func = fiber.vm.foreign[symbol.offset()];

        fiber.saveCallContext(ip + 1, ins.dst());


        try {
            func.call(fiber.vm, fiber);
            fiber.restoreCallContext();
            ip = ip + 1;
        } catch (Exception e) {
            ip = fiber.vm.trap(e, fiber, ip);
        }
        return ip;
    }
}
