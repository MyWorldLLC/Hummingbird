package myworld.hummingbird;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

import static myworld.hummingbird.Opcodes.*;

public class HummingbirdVM {

    protected final Executable exe;
    protected ByteBuffer memory;
    protected Fiber currentFiber;
    protected final Deque<Fiber> runQueue;
    protected final ForeignFunction[] foreign;

    public HummingbirdVM(Executable exe) {
        this.exe = exe;
        foreign = new ForeignFunction[(int)exe.foreignSymbols().count()];

        memory = ByteBuffer.allocate(1024);

        runQueue = new LinkedList<>();
    }

    public Object run(){

        spawn(null, null);

        currentFiber = nextFiber();
        var registers = currentFiber.registers;
        while(currentFiber != null){
            registers = currentFiber.registers;
            run(currentFiber);
            currentFiber = nextFiber();
        }

        var rType = TypeFlag.INT;
        if(exe.symbols().length > 0){
            rType = exe.symbols()[0].rType();
        }

        return switch (rType){
            case INT -> registers.ireg()[0];
            case FLOAT -> registers.freg()[0];
            case LONG -> registers.lreg()[0];
            case DOUBLE -> registers.dreg()[0];
            case STRING -> registers.sreg()[0];
            case OBJECT -> registers.oreg()[0];
            case VOID -> null;
        };
    }

    public Fiber spawn(Symbol entry, Registers initialState){
        return spawn(entry != null ? entry.offset() : 0, initialState);
    }

    protected Fiber spawn(int entry, Registers initialState){
        var registers = allocateRegisters(
                20,
                20,
                20,
                20,
                20,
                20
        );

        if(initialState != null){
            copyRegisters(initialState, registers);
        }

        var savedRegisters = new SavedRegisters(1000);
        savedRegisters.saveIp(Integer.MAX_VALUE);
        var fiber = new Fiber(registers, savedRegisters);

        savedRegisters.saveIp(entry);

        runQueue.push(fiber);

        return fiber;
    }

    protected Fiber nextFiber(){
        var it = runQueue.iterator();
        while(it.hasNext()){
            var fiber = it.next();
            if(fiber.getState() == Fiber.State.RUNNABLE){
                it.remove();
                return fiber;
            }
        }
        return null;
    }

    public void run(Fiber fiber) {

        var registers = fiber.registers;
        var ireg = registers.ireg();
        var freg = registers.freg();
        var lreg = registers.lreg();
        var dreg = registers.dreg();
        var sreg = registers.sreg();
        var oreg = registers.oreg();

        var savedRegisters = fiber.savedRegisters;
        var ip = savedRegisters.restoreIp();

        var instructions = exe.code();

        try {

            var stop = false;
            while (!stop && ip < instructions.length) {
                var ins = instructions[ip];
                var type = Opcodes.registerType(ins.dst());
                var dst = Opcodes.registerIndex(ins.dst());
                ip++;
                switch (ins.opcode()) {
                    case CONST -> {
                        switch (type){
                            case INT_T -> ireg[dst] = ins.src();
                            case FLOAT_T -> freg[dst] = Float.intBitsToFloat(ins.src());
                            case LONG_T -> lreg[dst] = longFromInts(ins.src(), ins.extra());
                            case DOUBLE_T -> dreg[dst] = Double.longBitsToDouble(longFromInts(ins.src(), ins.extra()));
                            case STRING_T -> sreg[dst] = constString(ins.src());
                            case OBJECT_T -> oreg[dst] = null;
                        }
                    }
                    case ADD -> {
                        switch (type){
                            case INT_T -> ireg[dst] = ireg[ins.src()] + ireg[ins.extra()];
                            case FLOAT_T -> freg[dst] = freg[ins.src()] + freg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] + lreg[ins.extra()];
                            case DOUBLE_T -> dreg[dst] = dreg[ins.src()] + dreg[ins.extra()];
                        }
                    }
                    case SUB -> {
                        switch (type){
                            case INT_T -> ireg[dst] = ireg[ins.src()] - ireg[ins.extra()];
                            case FLOAT_T -> freg[dst] = freg[ins.src()] - freg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] - lreg[ins.extra()];
                            case DOUBLE_T -> dreg[dst] = dreg[ins.src()] - dreg[ins.extra()];
                        }
                    }
                    case MUL -> {
                        switch (type){
                            case INT_T -> ireg[dst] = ireg[ins.src()] * ireg[ins.extra()];
                            case FLOAT_T -> freg[dst] = freg[ins.src()] * freg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] * lreg[ins.extra()];
                            case DOUBLE_T -> dreg[dst] = dreg[ins.src()] * dreg[ins.extra()];
                        }
                    }
                    case DIV -> {
                        switch (type){
                            case INT_T -> ireg[dst] = ireg[ins.src()] / ireg[ins.extra()];
                            case FLOAT_T -> freg[dst] = freg[ins.src()] / freg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] / lreg[ins.extra()];
                            case DOUBLE_T -> dreg[dst] = dreg[ins.src()] / dreg[ins.extra()];
                        }
                    }
                    case NEG -> {
                        switch (type){
                            case INT_T -> ireg[dst] = -ireg[ins.src()];
                            case FLOAT_T -> freg[dst] = -freg[ins.src()];
                            case LONG_T -> lreg[dst] = -lreg[ins.src()];
                            case DOUBLE_T -> dreg[dst] = -dreg[ins.src()];
                        }
                    }
                    case POW -> {
                        switch (type){
                            case INT_T -> ireg[dst] = (int) Math.pow(ireg[ins.src()], ireg[ins.extra()]);
                            case FLOAT_T -> freg[dst] = (float) Math.pow(freg[ins.src()], freg[ins.extra()]);
                            case LONG_T -> lreg[dst] = (long) Math.pow(lreg[ins.src()], lreg[ins.extra()]);
                            case DOUBLE_T -> dreg[dst] = Math.pow(dreg[ins.src()], dreg[ins.extra()]);
                        }
                    }
                    case BAND -> {
                        switch (type){
                            case INT_T -> ireg[dst] = ireg[ins.src()] & ireg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] & lreg[ins.extra()];
                        }
                    }
                    case BOR -> {
                        switch (type){
                            case INT_T -> ireg[dst] = ireg[ins.src()] | ireg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] | lreg[ins.extra()];
                        }
                    }
                    case BXOR -> {
                        switch (type){
                            case INT_T -> ireg[dst] = ireg[ins.src()] ^ ireg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] ^ lreg[ins.extra()];
                        }
                    }
                    case BNOT -> {
                        switch (type){
                            case INT_T -> ireg[dst] = ~ireg[ins.src()];
                            case LONG_T -> lreg[dst] = ~lreg[ins.src()];
                        }
                    }
                    case BLSHIFT -> {
                        switch (type){
                            case INT_T -> ireg[dst] = ireg[ins.src()] << ireg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] << lreg[ins.extra()];
                        }
                    }
                    case BSRSHIFT -> {
                        switch (type){
                            case INT_T -> ireg[dst] = ireg[ins.src()] >> ireg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] >> lreg[ins.extra()];
                        }
                    }
                    case BURSHIFT -> {
                        switch (type){
                            case INT_T -> ireg[dst] = ireg[ins.src()] >>> ireg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] >>> lreg[ins.extra()];
                        }
                    }
                    case CONV -> {
                        switch (type){
                            case INT_T -> toInt(registers, ins.src());
                            case FLOAT_T -> toFloat(registers, ins.src());
                            case LONG_T -> toLong(registers, ins.src());
                            case DOUBLE_T -> toDouble(registers, ins.src());
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
                    case RETURN ->{
                        ip = savedRegisters.restoreIp();
                    }
                    case COPY -> {
                        switch (type){
                            case INT_T -> ireg[dst] = ireg[ins.src()];
                            case FLOAT_T -> freg[dst] = freg[ins.src()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()];
                            case DOUBLE_T -> dreg[dst] = dreg[ins.src()];
                            case STRING_T -> sreg[dst] = sreg[ins.src()];
                            case OBJECT_T -> oreg[dst] = oreg[ins.src()];
                        }
                    }
                    case SAVE -> {
                        switch (type){
                            case INT_T -> savedRegisters.save(ireg, dst, ins.src());
                            case FLOAT_T -> savedRegisters.save(freg, dst, ins.src());
                            case LONG_T -> lreg[dst] = lreg[ins.src()];
                            case DOUBLE_T -> dreg[dst] = dreg[ins.src()];
                            case STRING_T -> sreg[dst] = sreg[ins.src()];
                            case OBJECT_T -> oreg[dst] = oreg[ins.src()];
                        }
                    }
                    case RESTORE -> {
                        switch (type){
                            case INT_T -> savedRegisters.restore(ireg, dst, ins.src());
                            case FLOAT_T -> savedRegisters.restore(freg, dst, ins.src());
                            case LONG_T -> lreg[dst] = lreg[ins.src()];
                            case DOUBLE_T -> dreg[dst] = dreg[ins.src()];
                            case STRING_T -> sreg[dst] = sreg[ins.src()];
                            case OBJECT_T -> oreg[dst] = oreg[ins.src()];
                        }
                    }
                    case IP -> {
                        ireg[dst] = ip;
                    }
                    case CALL -> {
                        savedRegisters.saveIp(ip);
                        ip = dst;
                    }
                    case FCALL -> {
                        var symbol = exe.symbols()[dst];
                        var func = foreign[symbol.offset()];
                        func.call(this, currentFiber);
                    }
                    case SPAWN -> {
                        oreg[dst] = spawn(ins.src(), registers);
                    }
                    case YIELD -> {
                        savedRegisters.saveIp(ip);
                        return;
                    }
                    case BLOCK -> {
                        currentFiber.setState(Fiber.State.BLOCKED);
                        savedRegisters.saveIp(ip);
                        return;
                    }
                    case UNBLOCK -> {
                        ((Fiber) oreg[dst]).setState(Fiber.State.RUNNABLE);
                    }
                    case WRITE -> {
                    }
                    case READ -> {}
                    case MWRITE -> {}
                    case MREAD -> {}
                    case GWRITE -> {}
                    case GREAD -> {}
                    case ALLOCATED -> {}
                    case RESIZE -> {}
                    case STR -> {}
                    case STR_LEN -> {}
                    case CHAR_AT -> {}
                    case CHARS -> {}
                    case CONCAT -> {}
                    case SUB_STR -> {}
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

    private static int toInt(Registers registers, int src){
        var type = Opcodes.registerType(src);
        src = Opcodes.registerIndex(src);
        return switch (type){
            case INT_T -> registers.ireg()[src];
            case FLOAT_T -> (int) registers.freg()[src];
            case LONG_T -> (int) registers.lreg()[src];
            case DOUBLE_T -> (int) registers.dreg()[src];
            default -> 0;
        };
    }

    private static float toFloat(Registers registers, int src){
        var type = Opcodes.registerType(src);
        src = Opcodes.registerIndex(src);
        return switch (type){
            case INT_T -> (float) registers.ireg()[src];
            case FLOAT_T -> registers.freg()[src];
            case LONG_T -> (float) registers.lreg()[src];
            case DOUBLE_T -> (float) registers.dreg()[src];
            default -> Float.NaN;
        };
    }

    private static long toLong(Registers registers, int src){
        var type = Opcodes.registerType(src);
        src = Opcodes.registerIndex(src);
        return switch (type){
            case INT_T -> registers.ireg()[src];
            case FLOAT_T -> (long) registers.freg()[src];
            case LONG_T -> registers.lreg()[src];
            case DOUBLE_T -> (long) registers.dreg()[src];
            default -> 0;
        };
    }

    private static double toDouble(Registers registers, int src){
        var type = Opcodes.registerType(src);
        src = Opcodes.registerIndex(src);
        return switch (type){
            case INT_T -> (double) registers.ireg()[src];
            case FLOAT_T -> (double) registers.freg()[src];
            case LONG_T -> (double) registers.lreg()[src];
            case DOUBLE_T -> registers.dreg()[src];
            default -> Double.NaN;
        };
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

    public static void copyRegisters(Registers from, Registers to){
        System.arraycopy(from.ireg(), 0, to.ireg(), 0, from.ireg().length);
        System.arraycopy(from.freg(), 0, to.freg(), 0, from.freg().length);
        System.arraycopy(from.lreg(), 0, to.lreg(), 0, from.lreg().length);
        System.arraycopy(from.dreg(), 0, to.dreg(), 0, from.dreg().length);
    }

}
