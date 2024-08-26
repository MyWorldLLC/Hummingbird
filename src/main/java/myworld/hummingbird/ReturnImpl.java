package myworld.hummingbird;

public class ReturnImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        var callCtx = fiber.callCtx;
        fiber.savedRegisters.restoreCallContext(callCtx);

        var result = callCtx.returnDest;
        var returnOffset = callCtx.registerOffset;
        var reg = fiber.registers;

        reg[returnOffset + result] = reg[regOffset + ins.dst()];

        ip = callCtx.ip;
        fiber.regOffset = returnOffset;
        return ip;
    }
}
