package myworld.hummingbird;

public interface OpcodeImpl {
    int apply(Opcode ins, long[] reg, int regOffset, int ip, Opcode[] instructions);

    static int chainNext(long[] reg, int regOffset, int ip, Opcode[] instructions){
        var next = instructions[ip + 1];
        return next.impl().apply(next, reg, regOffset, ip + 1, instructions);
    }
}
