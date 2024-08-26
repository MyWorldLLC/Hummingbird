package myworld.hummingbird;

public class ConstImpl implements OpcodeImpl {

    protected final long value;

    public ConstImpl(long value){
        this.value = value;
    }

    @Override
    public int apply(Opcode ins, long[] reg, int regOffset, int ip, Opcode[] instructions) {
        reg[regOffset + ins.dst()] = value;
        return OpcodeImpl.chainNext(reg, regOffset, ip, instructions);
    }
}
