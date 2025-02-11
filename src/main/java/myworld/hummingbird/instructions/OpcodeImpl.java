package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.HummingbirdException;
import myworld.hummingbird.Opcode;

public interface OpcodeImpl {
    int apply(Opcode[] instructions, Fiber fiber, Opcode ins, int[] registers, int ip);

    static int chainNext(Opcode[] instructions, Fiber fiber, int[] registers, int ip){
        try{
            var next = instructions[ip + 1];
            return next.impl().apply(instructions, fiber, next, registers, ip + 1);
        }catch (HummingbirdException e){
            throw e;
        }catch (Throwable t){
            return fiber.vm.trap(t, registers, ip);
        }

    }

    static int dispatchCall(Fiber fiber, Opcode ins, int[] registers, int ip, int symbolIndex){
        var symbol = fiber.exe.symbols()[symbolIndex];

        /*var callerOffset = regOffset;
        var callerParams = ins.extra();
        fiber.saveCallContext(ip + 1, regOffset, ins.dst());

        regOffset += symbol.registers();
        for(int i = 0; i < ins.extra1(); i++){
            registers[i] = registers[callerOffset + callerParams + i];
        }

        fiber.registerOffset = regOffset;*/
        fiber.prepareCall(ip, registers, ins.extra(), ins.extra1());
        ip = symbol.offset();
        fiber.ip = ip;
        return ip;
    }

    static int foreignCall(Fiber fiber, Opcode ins, int[] registers, int ip, int symbolIndex){
        var symbol = fiber.exe.symbols()[symbolIndex];
        var func = fiber.vm.foreign[symbol.offset()];

        //fiber.saveCallContext(ip + 1, regOffset, ins.dst());

        ip = ip + 1;
        try {
            func.call(fiber.vm, fiber);
        } catch (Exception e) {
            ip = fiber.vm.trap(e, registers, ip);
        }
        return ip;
    }
}
