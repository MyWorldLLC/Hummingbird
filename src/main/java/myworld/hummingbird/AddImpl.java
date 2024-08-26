package myworld.hummingbird;

public class AddImpl implements OpcodeImpl {
    @Override
    public int apply(Opcode ins, long[] registers, int offset, int ip) {
        registers[offset + ins.dst()] = registers[offset + ins.src()] + registers[offset + ins.extra()];
        return ip;
    }
}
