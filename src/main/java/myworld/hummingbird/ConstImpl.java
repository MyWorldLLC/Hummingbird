package myworld.hummingbird;

public class ConstImpl implements OpcodeImpl {

    protected final long value;

    public ConstImpl(long value){
        this.value = value;
    }

    @Override
    public int apply(Opcode ins, long[] reg, int regOffset, int ip) {
        reg[regOffset + ins.dst()] = value;
        return ip;
    }
}
