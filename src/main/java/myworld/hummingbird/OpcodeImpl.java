package myworld.hummingbird;

public interface OpcodeImpl {
    int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions);

    static int chainNext(Fiber fiber, int regOffset, int ip, Opcode[] instructions){
        var next = instructions[ip + 1];
        return next.impl().apply(fiber, next, regOffset, ip + 1, instructions);
    }
}
