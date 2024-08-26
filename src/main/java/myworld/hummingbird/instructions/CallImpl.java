package myworld.hummingbird.instructions;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;

public class CallImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var symbol = fiber.exe.symbols()[ins.src()];

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
}
