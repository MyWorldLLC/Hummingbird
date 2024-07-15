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

    public Object run() {

        var ip = 0;
        var instructions = exe.code();

        try {

            var registers = new Registers(
                    new int[3],
                    new long[0],
                    new float[0],
                    new double[0],
                    new String[0],
                    new Object[0]
            );

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
                            case FLOAT_T -> registers.freg()[dst] = ins.src();
                            case LONG_T -> registers.lreg()[dst] = ins.src();
                            case DOUBLE_T -> registers.dreg()[dst] = ins.src();
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
                        }else{
                            stop = true;
                        }

                    }
                    case FCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if(condFloats(cond, registers, dst, src)){
                            ip = ins.extra();
                        }else{
                            stop = true;
                        }

                    }
                    case LCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if(condLongs(cond, registers, dst, src)){
                            ip = ins.extra();
                        }else{
                            stop = true;
                        }

                    }
                    case DCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if(condDoubles(cond, registers, dst, src)){
                            ip = ins.extra();
                        }else{
                            stop = true;
                        }

                    }
                    case SCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if(condStrings(cond, registers, dst, src)){
                            ip = ins.extra();
                        }else{
                            stop = true;
                        }
                    }
                    case OCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if(condObjects(cond, registers, dst, src)){
                            ip = ins.extra();
                        }else{
                            stop = true;
                        }
                    }
                    case RETURN -> {
                        switch (type){
                            case INT_T -> {
                                // TODO - instead of 'return' keyword, copy value
                                // to parent frame and unwind stack by one frame
                                return registers.ireg()[dst];
                            }
                            case FLOAT_T -> {
                                return registers.freg()[dst];
                            }
                            case LONG_T -> {
                                return registers.lreg()[dst];
                            }
                            case DOUBLE_T -> {
                                return registers.dreg()[dst];
                            }
                            case STRING_T -> {
                                return registers.sreg()[dst];
                            }
                            case OBJECT_T -> {
                                return registers.oreg()[dst];
                            }
                        }
                    }
                }
            }
            return registers.ireg()[0];
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

}
