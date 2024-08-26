package myworld.hummingbird;

public class ReturnImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode ins, long[] reg, int regOffset, int ip) {
        return ip;
    }
}
