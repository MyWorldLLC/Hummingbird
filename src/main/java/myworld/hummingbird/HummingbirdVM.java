package myworld.hummingbird;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static myworld.hummingbird.Opcodes.*;

public class HummingbirdVM {

    protected final Executable exe;
    protected ByteBuffer memory;

    public HummingbirdVM(Executable exe) {
        this.exe = exe;

        memory = ByteBuffer.allocate(1024);
    }

    protected Fiber fiber;

    public Object run(){
        var symbol = exe.symbols()[0];
        var frame = Frame.hostFrame(symbol);
        run(frame);

        return switch (symbol.rType()){
            case INT -> frame.registers().ireg()[0];
            case FLOAT -> frame.registers().freg()[0];
            case LONG -> frame.registers().lreg()[0];
            case DOUBLE -> frame.registers().dreg()[0];
            case STRING -> frame.registers().sreg()[0];
            case OBJECT -> frame.registers().oreg()[0];
            case VOID -> null;
        };
    }

    public void run(Frame parent) {

        var ip = 0;
        var instructions = exe.code();

        try {

            var registers = allocateRegisters(
                    parent.symbol().registers().intCounts(),
                    parent.symbol().registers().floatCounts(),
                    parent.symbol().registers().longCounts(),
                    parent.symbol().registers().doubleCounts(),
                    parent.symbol().registers().stringCounts(),
                    parent.symbol().registers().objectCounts()
            );

            Frame frame = new Frame(parent, registers, parent.symbol(), Params.zeroes());

            var stop = false;
            while (!stop && ip < instructions.length) {
                var ins = instructions[ip];
                var type = Opcodes.registerType(ins.dst());
                var dst = Opcodes.registerIndex(ins.dst());
                ip++;
                switch (ins.opcode()) {
                    case CONST -> {
                        switch (type){
                            case INT_T -> registers.ireg()[dst] = ins.src();
                            case FLOAT_T -> registers.freg()[dst] = Float.intBitsToFloat(ins.src());
                            case LONG_T -> registers.lreg()[dst] = ins.src(); // TODO
                            case DOUBLE_T -> registers.dreg()[dst] = ins.src(); // TODO
                            case STRING_T -> registers.sreg()[dst] = constString(ins.src());
                            case OBJECT_T -> registers.oreg()[dst] = null;
                        }
                    }
                    case ADD -> {
                        switch (type){
                            case INT_T -> registers.ireg()[dst] += registers.ireg()[ins.src()];
                            case FLOAT_T -> registers.freg()[dst] += registers.freg()[ins.src()];
                            case LONG_T -> registers.lreg()[dst] += registers.lreg()[ins.src()];
                            case DOUBLE_T -> registers.dreg()[dst] += registers.dreg()[ins.src()];
                        }
                    }
                    case SUB -> {
                        switch (type){
                            case INT_T -> registers.ireg()[dst] -= registers.ireg()[ins.src()];
                            case FLOAT_T -> registers.freg()[dst] -= registers.freg()[ins.src()];
                            case LONG_T -> registers.lreg()[dst] -= registers.lreg()[ins.src()];
                            case DOUBLE_T -> registers.dreg()[dst] -= registers.dreg()[ins.src()];
                        }
                    }
                    case MUL -> {
                        switch (type){
                            case INT_T -> registers.ireg()[dst] *= registers.ireg()[ins.src()];
                            case FLOAT_T -> registers.freg()[dst] *= registers.freg()[ins.src()];
                            case LONG_T -> registers.lreg()[dst] *= registers.lreg()[ins.src()];
                            case DOUBLE_T -> registers.dreg()[dst] *= registers.dreg()[ins.src()];
                        }
                    }
                    case DIV -> {
                        switch (type){
                            case INT_T -> registers.ireg()[dst] /= registers.ireg()[ins.src()];
                            case FLOAT_T -> registers.freg()[dst] /= registers.freg()[ins.src()];
                            case LONG_T -> registers.lreg()[dst] /= registers.lreg()[ins.src()];
                            case DOUBLE_T -> registers.dreg()[dst] /= registers.dreg()[ins.src()];
                        }
                    }
                    case NEG -> {
                        switch (type){
                            case INT_T -> registers.ireg()[dst] = -registers.ireg()[dst];
                            case FLOAT_T -> registers.freg()[dst] = -registers.freg()[dst];
                            case LONG_T -> registers.lreg()[dst] = -registers.lreg()[dst];
                            case DOUBLE_T -> registers.dreg()[dst] = -registers.dreg()[dst];
                        }
                    }
                    case POW -> {
                        switch (type){
                            case INT_T -> registers.ireg()[dst] = (int) Math.pow(registers.ireg()[dst], registers.ireg()[ins.src()]);
                            case FLOAT_T -> registers.freg()[dst] = (float) Math.pow(registers.freg()[dst], registers.freg()[ins.src()]);
                            case LONG_T -> registers.lreg()[dst] = (long) Math.pow(registers.lreg()[dst], registers.lreg()[ins.src()]);
                            case DOUBLE_T -> registers.dreg()[dst] = Math.pow(registers.dreg()[dst], registers.dreg()[ins.src()]);
                        }
                    }
                    case GOTO -> {
                        ip = dst;
                    }
                    case ICOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if(condInts(cond, registers, dst, src)){
                            ip = ins.extra();
                        }
                    }
                    case FCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if(condFloats(cond, registers, dst, src)){
                            ip = ins.extra();
                        }
                    }
                    case LCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if(condLongs(cond, registers, dst, src)){
                            ip = ins.extra();
                        }
                    }
                    case DCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if(condDoubles(cond, registers, dst, src)){
                            ip = ins.extra();
                        }
                    }
                    case SCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if(condStrings(cond, registers, dst, src)){
                            ip = ins.extra();
                        }
                    }
                    case OCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if(condObjects(cond, registers, dst, src)){
                            ip = ins.extra();
                        }
                    }
                    case RETURN -> {
                        var target = frame.returnTarget();
                        switch (type){
                            case INT_T -> frame.parent.registers.ireg()[target] = registers.ireg()[dst];
                            case FLOAT_T -> frame.parent.registers.freg()[target] = registers.freg()[dst];
                            case LONG_T -> frame.parent.registers.lreg()[target] = registers.lreg()[dst];
                            case DOUBLE_T -> frame.parent.registers.dreg()[target] = registers.dreg()[dst];
                            case STRING_T -> frame.parent.registers.sreg()[target] = registers.sreg()[dst];
                            case OBJECT_T -> frame.parent.registers.oreg()[target] = registers.oreg()[dst];
                        }
                        frame = frame.parent();
                        ip = frame.ip();
                        registers = frame.registers;
                    }
                    case PARAM -> {
                        // TODO - support offset from register in addition to immediate
                        var paramOffsets = frame.paramOffsets();
                        switch (type){
                            case INT_T -> registers.ireg()[dst] = frame.parent.registers.ireg()[paramOffsets.iParam() + ins.src()];
                            case FLOAT_T -> registers.freg()[dst] = frame.parent.registers.freg()[paramOffsets.fParam() + ins.src()];
                            case LONG_T -> registers.lreg()[dst] = frame.parent.registers.lreg()[paramOffsets.lParam() + ins.src()];
                            case DOUBLE_T -> registers.dreg()[dst] = frame.parent.registers.dreg()[paramOffsets.dParam() + ins.src()];
                            case STRING_T -> registers.sreg()[dst] = frame.parent.registers.sreg()[paramOffsets.sParam() + ins.src()];
                            case OBJECT_T -> registers.oreg()[dst] = frame.parent.registers.oreg()[paramOffsets.oParam() + ins.src()];
                        }
                    }
                    case CALL -> {
                        var cSymbol = exe.symbols()[ins.src()];
                        var cReg = allocateRegisters(
                                cSymbol.registers().intCounts(),
                                cSymbol.registers().floatCounts(),
                                cSymbol.registers().longCounts(),
                                cSymbol.registers().doubleCounts(),
                                cSymbol.registers().stringCounts(),
                                cSymbol.registers().objectCounts()
                        );
                        var cFrame = new Frame(frame, cReg, cSymbol, new Params(ins.extra(), ins.extra1(), ins.extra2(), ins.extra3(), ins.extra4(), ins.extra5()));
                        frame.setReturnTarget(dst);
                        frame.setIp(ip);
                        frame = cFrame;
                        registers = cReg;
                        ip = cSymbol.offset(); // TODO - support foreign function calls
                    }
                    case COPY -> {
                        switch (type){
                            case INT_T -> registers.ireg()[dst] = registers.ireg()[ins.src()];
                            case FLOAT_T -> registers.freg()[dst] = registers.freg()[ins.src()];
                            case LONG_T -> registers.lreg()[dst] = registers.lreg()[ins.src()];
                            case DOUBLE_T -> registers.dreg()[dst] = registers.dreg()[ins.src()];
                            case STRING_T -> registers.sreg()[dst] = registers.sreg()[ins.src()];
                            case OBJECT_T -> registers.oreg()[dst] = registers.oreg()[ins.src()];
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failure at ip: " + (ip - 1));
            System.out.println(Arrays.toString(instructions));
            throw e;
        }
    }

    public String constString(int address) {
        if(address < 0){
            return null;
        }
        int length = Math.min(memory.getInt(address), memory.capacity() / 2);
        char[] characters = new char[length];
        memory.slice(address, length * 2).asCharBuffer().get(characters);
        return new String(characters);
    }

    public static long longFromInts(int high, int low) {
        return ((long) high << 32) | ((long) low);
    }

    private static boolean condInts(int cond, Registers registers, int dst, int src){
        switch (cond){
            case COND_LT -> {
                return registers.ireg()[dst] < registers.ireg()[src];
            }
            case COND_LE -> {
                return registers.ireg()[dst] <= registers.ireg()[src];
            }
            case COND_EQ -> {
                return registers.ireg()[dst] == registers.ireg()[src];
            }
            case COND_GE -> {
                return registers.ireg()[dst] >= registers.ireg()[src];
            }
            case COND_GT -> {
                return registers.ireg()[dst] > registers.ireg()[src];
            }
        }
        return false;
    }

    private static boolean condFloats(int cond, Registers registers, int dst, int src){
        switch (cond){
            case COND_LT -> {
                return registers.freg()[dst] < registers.freg()[src];
            }
            case COND_LE -> {
                return registers.freg()[dst] <= registers.freg()[src];
            }
            case COND_EQ -> {
                return registers.freg()[dst] == registers.freg()[src];
            }
            case COND_GE -> {
                return registers.freg()[dst] >= registers.freg()[src];
            }
            case COND_GT -> {
                return registers.freg()[dst] > registers.freg()[src];
            }
        }
        return false;
    }

    private static boolean condLongs(int cond, Registers registers, int dst, int src){
        switch (cond){
            case COND_LT -> {
                return registers.lreg()[dst] < registers.lreg()[src];
            }
            case COND_LE -> {
                return registers.lreg()[dst] <= registers.lreg()[src];
            }
            case COND_EQ -> {
                return registers.lreg()[dst] == registers.lreg()[src];
            }
            case COND_GE -> {
                return registers.lreg()[dst] >= registers.lreg()[src];
            }
            case COND_GT -> {
                return registers.lreg()[dst] > registers.lreg()[src];
            }
        }
        return false;
    }

    private static boolean condDoubles(int cond, Registers registers, int dst, int src){
        switch (cond){
            case COND_LT -> {
                return registers.dreg()[dst] < registers.dreg()[src];
            }
            case COND_LE -> {
                return registers.dreg()[dst] <= registers.dreg()[src];
            }
            case COND_EQ -> {
                return registers.dreg()[dst] == registers.dreg()[src];
            }
            case COND_GE -> {
                return registers.dreg()[dst] >= registers.dreg()[src];
            }
            case COND_GT -> {
                return registers.dreg()[dst] > registers.dreg()[src];
            }
        }
        return false;
    }

    private static boolean condStrings(int cond, Registers registers, int dst, int src){
        var test = cond != COND_NULL ? registers.sreg()[dst].compareTo(registers.sreg()[src]) : 0;
        switch (cond){
            case COND_LT -> {
                return test < 0;
            }
            case COND_LE -> {
                return test <= 0;
            }
            case COND_EQ -> {
                return test == 0;
            }
            case COND_GE -> {
                return test >= 0;
            }
            case COND_GT -> {
                return test > 0;
            }
            case COND_NULL -> {
                return registers.sreg()[dst] == null;
            }
        }
        return false;
    }

    private static boolean condObjects(int cond, Registers registers, int dst, int src){
        switch (cond){
            case COND_EQ -> {
                return registers.oreg()[dst] == registers.oreg()[src];
            }
            case COND_NULL -> {
                return registers.oreg()[dst] == null;
            }
        }
        return false;
    }

    private static Registers allocateRegisters(int i, int f, int l, int d, int s, int o){
        return new Registers(
                new int[i],
                new float[f],
                new long[l],
                new double[d],
                new String[s],
                new Object[o]
        );
    }

}
