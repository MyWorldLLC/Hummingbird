package myworld.hummingbird;

public class ConstImpl implements OpcodeImpl {

    protected final long value;

    public ConstImpl(long value){
        this.value = value;
    }

    @Override
    public int apply(Fiber fiber, Opcode ins, int regOffset, int ip, Opcode[] instructions) {
        fiber.registers[regOffset + ins.dst()] = value;
        return OpcodeImpl.chainNext(fiber, regOffset, ip, instructions);
    }
}
