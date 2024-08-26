package myworld.hummingbird;

public interface OpcodeImpl {
    int apply(Opcode ins, long[] reg, int regOffset, int ip, Opcode next);
}
