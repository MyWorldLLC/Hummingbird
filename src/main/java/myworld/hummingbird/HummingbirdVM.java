package myworld.hummingbird;

import java.nio.ByteBuffer;

import static myworld.hummingbird.CoreOpcodes.*;

public class HummingbirdVM {

    protected final Executable exe;
    protected ByteBuffer memory;

    public HummingbirdVM(Executable exe){
        this.exe = exe;

        memory = ByteBuffer.allocate(1024);
    }

    protected Fiber fiber;

    public Object run(){

        var registers = new Registers(
                new int[3],
                new long[0],
                new float[0],
                new double[0],
                new String[0],
                new Object[0]
        );

        var instructions = exe.code();

        var stop = false;
        var ip = 0;
        while(!stop && ip < instructions.length){
            var ins = instructions[ip];
            switch (ins.opcode()){
                case ADD -> {
                    var dst = Opcode.registerIndex(ins.dst());
                    switch(Opcode.registerType(ins.dst())){
                        case 0 -> registers.ireg()[dst] += registers.ireg()[ins.src()];
                        case 1 -> registers.freg()[dst] += registers.freg()[ins.src()];
                        case 2 -> registers.lreg()[dst] += registers.lreg()[ins.src()];
                        case 3 -> registers.dreg()[dst] += registers.dreg()[ins.src()];
                    }
                    ip++;
                }
                case COND -> {
                    var dst = Opcode.registerIndex(ins.dst());
                    var src = Opcode.registerIndex(ins.src());
                    var type = Opcode.registerType(ins.dst());
                    var cond = Opcode.registerType(ins.src());
                    var test = switch (type){
                        case 0 -> condInts(cond, registers, dst, src);
                        case 1 -> condFloats(cond, registers, dst, src);
                        case 2 -> condLongs(cond, registers, dst, src);
                        case 3 -> condDoubles(cond, registers, dst, src);
                        default -> false;
                    };

                    if(test){
                        ip = ins.extra();
                    }else{
                        stop = true;
                    }

                }
                case CONST -> {
                    var dst = Opcode.registerIndex(ins.dst());
                    switch(Opcode.registerType(ins.dst())){
                        case 0 -> registers.ireg()[dst] = ins.src();
                        case 1 -> registers.freg()[dst] = Float.intBitsToFloat(ins.src());
                        case 2 -> registers.lreg()[dst] = longFromInts(ins.src(), ins.extra());
                        case 3 -> registers.dreg()[dst] = Double.longBitsToDouble(longFromInts(ins.src(), ins.extra()));
                        case 4 -> registers.sreg()[dst] = constString(ins.src());
                    }
                    ip++;
                }
                case RETURN -> {
                    // TODO - unwind VM call stack and place result in call target register
                    return registers.ireg()[0];
                }
            }
        }
        return registers.ireg()[0];
    }

    public String constString(int address){
        int length = Math.min(memory.getInt(address), memory.capacity()/2);
        char[] characters = new char[length];
        memory.slice(address, length * 2).asCharBuffer().get(characters);
        return new String(characters);
    }

    public static long longFromInts(int high, int low){
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

}
