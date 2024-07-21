package myworld.hummingbird;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;

import static myworld.hummingbird.Opcodes.*;

public class HummingbirdVM {

    protected final Executable exe;
    protected final MemoryLimits limits;
    protected ByteBuffer memory;
    protected Object[] objMemory;
    protected Fiber currentFiber;
    protected int trapTableAddr = -1;
    protected int trapHandlerCount = 0;
    protected List<Function<Throwable, Integer>> trapCodes;
    protected final Deque<Fiber> runQueue;
    protected final ForeignFunction[] foreign;

    public HummingbirdVM(Executable exe) {
        this(exe, new MemoryLimits(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    public HummingbirdVM(Executable exe, MemoryLimits limits) {
        this.exe = exe;
        this.limits = limits;
        foreign = new ForeignFunction[(int) exe.foreignSymbols().count()];

        memory = ByteBuffer.allocate(1024);
        objMemory = new Object[10];

        runQueue = new LinkedList<>();
        trapCodes = new ArrayList<>();
    }

    public Object run() {

        spawn(null, null);

        currentFiber = nextFiber();
        var registers = currentFiber.registers;
        while (currentFiber != null) {
            registers = currentFiber.registers;
            run(currentFiber);
            currentFiber = nextFiber();
        }

        var rType = TypeFlag.INT;
        if (exe.symbols().length > 0) {
            rType = exe.symbols()[0].rType();
        }

        return switch (rType) {
            case INT -> registers.ireg()[0];
            case FLOAT -> registers.freg()[0];
            case LONG -> registers.lreg()[0];
            case DOUBLE -> registers.dreg()[0];
            case OBJECT -> registers.oreg()[0];
            case VOID -> null;
        };
    }

    public Fiber spawn(Symbol entry, Registers initialState) {
        return spawn(entry != null ? entry.offset() : 0, initialState);
    }

    protected Fiber spawn(int entry, Registers initialState) {
        var registers = allocateRegisters(
                20,
                20,
                20,
                20,
                20
        );

        if (initialState != null) {
            copyRegisters(initialState, registers);
        }

        var savedRegisters = new SavedRegisters(1000);
        savedRegisters.saveIp(Integer.MAX_VALUE);
        var fiber = new Fiber(registers, savedRegisters);

        savedRegisters.saveIp(entry);

        runQueue.push(fiber);

        return fiber;
    }

    protected Fiber nextFiber() {
        var it = runQueue.iterator();
        while (it.hasNext()) {
            var fiber = it.next();
            if (fiber.getState() == Fiber.State.RUNNABLE) {
                it.remove();
                return fiber;
            }
        }
        return null;
    }

    public void run(Fiber fiber) throws HummingbirdException {

        var registers = fiber.registers;
        var ireg = registers.ireg();
        var freg = registers.freg();
        var lreg = registers.lreg();
        var dreg = registers.dreg();
        var oreg = registers.oreg();

        var savedRegisters = fiber.savedRegisters;
        var ip = savedRegisters.restoreIp();

        var instructions = exe.code();
        var stop = false;
        while (!stop && ip < instructions.length) {
            var ins = instructions[ip];
            var type = Opcodes.registerType(ins.dst());
            var dst = Opcodes.registerIndex(ins.dst());
            ip++;
            try {
                switch (ins.opcode()) {
                    case CONST -> {
                        switch (type) {
                            case INT_T -> ireg[dst] = ins.src();
                            case FLOAT_T -> freg[dst] = Float.intBitsToFloat(ins.src());
                            case LONG_T -> lreg[dst] = longFromInts(ins.src(), ins.extra());
                            case DOUBLE_T -> dreg[dst] = Double.longBitsToDouble(longFromInts(ins.src(), ins.extra()));
                            case OBJECT_T -> oreg[dst] = null;
                        }
                    }
                    case ADD -> {
                        switch (type) {
                            case INT_T -> ireg[dst] = ireg[ins.src()] + ireg[ins.extra()];
                            case FLOAT_T -> freg[dst] = freg[ins.src()] + freg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] + lreg[ins.extra()];
                            case DOUBLE_T -> dreg[dst] = dreg[ins.src()] + dreg[ins.extra()];
                        }
                    }
                    case SUB -> {
                        switch (type) {
                            case INT_T -> ireg[dst] = ireg[ins.src()] - ireg[ins.extra()];
                            case FLOAT_T -> freg[dst] = freg[ins.src()] - freg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] - lreg[ins.extra()];
                            case DOUBLE_T -> dreg[dst] = dreg[ins.src()] - dreg[ins.extra()];
                        }
                    }
                    case MUL -> {
                        switch (type) {
                            case INT_T -> ireg[dst] = ireg[ins.src()] * ireg[ins.extra()];
                            case FLOAT_T -> freg[dst] = freg[ins.src()] * freg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] * lreg[ins.extra()];
                            case DOUBLE_T -> dreg[dst] = dreg[ins.src()] * dreg[ins.extra()];
                        }
                    }
                    case DIV -> {
                        try{
                            switch (type) {
                                case INT_T -> ireg[dst] = ireg[ins.src()] / ireg[ins.extra()];
                                case FLOAT_T -> freg[dst] = freg[ins.src()] / freg[ins.extra()];
                                case LONG_T -> lreg[dst] = lreg[ins.src()] / lreg[ins.extra()];
                                case DOUBLE_T -> dreg[dst] = dreg[ins.src()] / dreg[ins.extra()];
                            }
                        }catch(ArithmeticException ex){
                            trap(Traps.DIV_BY_ZERO, registers, ip);
                        }
                    }
                    case NEG -> {
                        switch (type) {
                            case INT_T -> ireg[dst] = -ireg[ins.src()];
                            case FLOAT_T -> freg[dst] = -freg[ins.src()];
                            case LONG_T -> lreg[dst] = -lreg[ins.src()];
                            case DOUBLE_T -> dreg[dst] = -dreg[ins.src()];
                        }
                    }
                    case POW -> {
                        switch (type) {
                            case INT_T -> ireg[dst] = (int) Math.pow(ireg[ins.src()], ireg[ins.extra()]);
                            case FLOAT_T -> freg[dst] = (float) Math.pow(freg[ins.src()], freg[ins.extra()]);
                            case LONG_T -> lreg[dst] = (long) Math.pow(lreg[ins.src()], lreg[ins.extra()]);
                            case DOUBLE_T -> dreg[dst] = Math.pow(dreg[ins.src()], dreg[ins.extra()]);
                        }
                    }
                    case BAND -> {
                        switch (type) {
                            case INT_T -> ireg[dst] = ireg[ins.src()] & ireg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] & lreg[ins.extra()];
                        }
                    }
                    case BOR -> {
                        switch (type) {
                            case INT_T -> ireg[dst] = ireg[ins.src()] | ireg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] | lreg[ins.extra()];
                        }
                    }
                    case BXOR -> {
                        switch (type) {
                            case INT_T -> ireg[dst] = ireg[ins.src()] ^ ireg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] ^ lreg[ins.extra()];
                        }
                    }
                    case BNOT -> {
                        switch (type) {
                            case INT_T -> ireg[dst] = ~ireg[ins.src()];
                            case LONG_T -> lreg[dst] = ~lreg[ins.src()];
                        }
                    }
                    case BLSHIFT -> {
                        switch (type) {
                            case INT_T -> ireg[dst] = ireg[ins.src()] << ireg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] << lreg[ins.extra()];
                        }
                    }
                    case BSRSHIFT -> {
                        switch (type) {
                            case INT_T -> ireg[dst] = ireg[ins.src()] >> ireg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] >> lreg[ins.extra()];
                        }
                    }
                    case BURSHIFT -> {
                        switch (type) {
                            case INT_T -> ireg[dst] = ireg[ins.src()] >>> ireg[ins.extra()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()] >>> lreg[ins.extra()];
                        }
                    }
                    case CONV -> {
                        switch (type) {
                            case INT_T -> toInt(registers, ins.src());
                            case FLOAT_T -> toFloat(registers, ins.src());
                            case LONG_T -> toLong(registers, ins.src());
                            case DOUBLE_T -> toDouble(registers, ins.src());
                        }
                    }
                    case GOTO -> {
                        ip = dst;
                    }
                    case JMP -> {
                        ip = ireg[dst];
                    }
                    case ICOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if (condInts(cond, registers, dst, src)) {
                            ip = ins.extra();
                        }
                    }
                    case FCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if (condFloats(cond, registers, dst, src)) {
                            ip = ins.extra();
                        }
                    }
                    case LCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if (condLongs(cond, registers, dst, src)) {
                            ip = ins.extra();
                        }
                    }
                    case DCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if (condDoubles(cond, registers, dst, src)) {
                            ip = ins.extra();
                        }
                    }
                    case OCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if (condObjects(cond, registers, dst, src)) {
                            ip = ins.extra();
                        }
                    }
                    case RETURN -> {
                        ip = savedRegisters.restoreIp();
                    }
                    case COPY -> {
                        switch (type) {
                            case INT_T -> ireg[dst] = ireg[ins.src()];
                            case FLOAT_T -> freg[dst] = freg[ins.src()];
                            case LONG_T -> lreg[dst] = lreg[ins.src()];
                            case DOUBLE_T -> dreg[dst] = dreg[ins.src()];
                            case OBJECT_T -> oreg[dst] = oreg[ins.src()];
                        }
                    }
                    case SAVE -> {
                        switch (type) {
                            case INT_T -> savedRegisters.save(ireg, dst, ins.src());
                            case FLOAT_T -> savedRegisters.save(freg, dst, ins.src());
                            case LONG_T -> lreg[dst] = lreg[ins.src()];
                            case DOUBLE_T -> dreg[dst] = dreg[ins.src()];
                            case OBJECT_T -> oreg[dst] = oreg[ins.src()];
                        }
                    }
                    case RESTORE -> {
                        switch (type) {
                            case INT_T -> savedRegisters.restore(ireg, dst, ins.src());
                            case FLOAT_T -> savedRegisters.restore(freg, dst, ins.src());
                            case LONG_T -> lreg[dst] = lreg[ins.src()];
                            case DOUBLE_T -> dreg[dst] = dreg[ins.src()];
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
                    case DCALL -> {
                        savedRegisters.saveIp(ip);
                        ip = ireg[dst];
                    }
                    case FCALL -> {
                        var symbol = exe.symbols()[dst];
                        var func = foreign[symbol.offset()];
                        func.call(this, currentFiber);
                    }
                    case DFCALL -> {
                        var symbol = exe.symbols()[ireg[dst]];
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
                        var wType = Opcodes.registerType(ins.src());
                        var src = Opcodes.registerIndex(ins.src());
                        var addr = ireg[ins.dst()];
                        switch (wType) {
                            case INT_T -> memory.putInt(addr, ireg[src]);
                            case FLOAT_T -> memory.putFloat(addr, freg[src]);
                            case LONG_T -> memory.putLong(addr, lreg[src]);
                            case DOUBLE_T -> memory.putDouble(addr, dreg[src]);
                            case OBJECT_T -> objMemory[addr] = oreg[src];
                        }
                    }
                    case READ -> {
                        switch (type) {
                            case INT_T -> ireg[dst] = memory.getInt(ireg[ins.src()]);
                            case FLOAT_T -> freg[dst] = memory.getFloat(ireg[ins.src()]);
                            case LONG_T -> lreg[dst] = memory.getLong(ireg[ins.src()]);
                            case DOUBLE_T -> dreg[dst] = memory.getDouble(ireg[ins.src()]);
                            case OBJECT_T -> oreg[dst] = objMemory[ireg[ins.src()]];
                        }
                    }
                    case SWRITE -> {
                        var wType = Opcodes.registerType(ins.src());
                        var src = Opcodes.registerIndex(ins.src());
                        var addr = ireg[ins.dst()];
                        long value = switch (wType) {
                            case INT_T -> ireg[src];
                            case LONG_T -> lreg[src];
                            default -> 0;
                        };
                        switch (ins.extra()) {
                            case BYTE_T -> memory.put(addr, (byte) value);
                            case CHAR_T -> memory.putChar(addr, (char) value);
                            case SHORT_T -> memory.putShort(addr, (short) value);
                        }
                    }
                    case SREAD -> {
                        var addr = ireg[ins.src()];
                        int value = switch (ins.extra()) {
                            case BYTE_T -> memory.get(addr);
                            case CHAR_T -> memory.getChar(addr);
                            case SHORT_T -> memory.getShort(addr);
                            default -> 0;
                        };
                        switch (type) {
                            case INT_T -> ireg[dst] = value;
                            case LONG_T -> lreg[dst] = value;
                        }
                    }
                    case GWRITE -> {
                        var wType = Opcodes.registerType(ins.src());
                        var src = Opcodes.registerIndex(ins.src());
                        var addr = ireg[ins.dst()];
                        memory.put(ireg[ins.extra()], (byte) ins.extra1());
                        switch (wType) {
                            case INT_T -> memory.putInt(addr, ireg[src]);
                            case FLOAT_T -> memory.putFloat(addr, freg[src]);
                            case LONG_T -> memory.putLong(addr, lreg[src]);
                            case DOUBLE_T -> memory.putDouble(addr, dreg[src]);
                            case OBJECT_T -> objMemory[addr] = oreg[src];
                        }
                    }
                    case GREAD -> {
                        var guard = memory.get(ireg[ins.extra()]);
                        if (guard != 0) {
                            ip = ins.extra1();
                        }
                        switch (type) {
                            case INT_T -> ireg[dst] = memory.getInt(ireg[ins.src()]);
                            case FLOAT_T -> freg[dst] = memory.getFloat(ireg[ins.src()]);
                            case LONG_T -> lreg[dst] = memory.getLong(ireg[ins.src()]);
                            case DOUBLE_T -> dreg[dst] = memory.getDouble(ireg[ins.src()]);
                            case OBJECT_T -> oreg[dst] = objMemory[ireg[ins.src()]];
                        }
                    }
                    case MEM_COPY -> {
                        var start = ireg[ins.src()];
                        var end = ireg[ins.extra()];
                        memory.put(ireg[dst], memory.slice(start, end), 0, end - start);
                    }
                    case OBJ_COPY -> {
                        var start = ireg[ins.src()];
                        var end = ireg[ins.extra()];
                        System.arraycopy(objMemory, start, objMemory, ireg[dst], end - start);
                    }
                    case ALLOCATED -> {
                        switch (ins.src()) {
                            case OBJECT_T -> ireg[dst] = objMemory.length;
                            default -> ireg[dst] = memory.capacity();
                        }
                    }
                    case RESIZE -> {
                        var size = Math.min(ireg[ins.dst()], limits.bytes());
                        var next = ByteBuffer.allocate(size);
                        next.put(0, memory, 0, Math.min(size, memory.capacity()));
                        memory = next;
                    }
                    case OBJ_RESIZE -> {
                        var size = Math.min(ireg[ins.dst()], limits.objects());
                        var next = new Object[size];
                        System.arraycopy(objMemory, 0, next, 0, Math.min(size, objMemory.length));
                        objMemory = next;
                    }
                    case STR -> {
                        var sType = Opcodes.registerType(ins.src());
                        var src = Opcodes.registerIndex(ins.src());
                        switch (sType) {
                            case INT_T -> oreg[dst] = Integer.toString(ireg[src]);
                            case FLOAT_T -> oreg[dst] = Float.toString(freg[src]);
                            case LONG_T -> oreg[dst] = Long.toString(lreg[src]);
                            case DOUBLE_T -> oreg[dst] = Double.toString(dreg[src]);
                            case OBJECT_T -> oreg[dst] = objectToString(oreg[src]);
                        }
                    }
                    case STR_LEN -> {
                        if (oreg[ins.src()] instanceof String s) {
                            ireg[dst] = s.length();
                        } else {
                            ireg[dst] = 0;
                        }
                    }
                    case CHAR_AT -> {
                        if (oreg[ins.src()] instanceof String s) {
                            ireg[dst] = s.charAt(ireg[ins.extra()]);
                        } else {
                            ireg[dst] = 0;
                        }
                    }
                    case TO_CHARS -> {
                        var charBuf = memory.asCharBuffer();
                        var address = ireg[dst];
                        if (oreg[ins.src()] instanceof String s) {
                            var chars = s.toCharArray();
                            memory.putInt(address, chars.length);
                            charBuf.put((address + 4) / 2, chars);
                        }
                    }
                    case FROM_CHARS -> {
                        var address = ireg[ins.src()];
                        oreg[dst] = readString(address);
                    }
                    case CONCAT -> {
                        var a = oreg[ins.src()];
                        var b = oreg[ins.extra()];
                        oreg[dst] = objectToString(a) + objectToString(b);
                    }
                    case SUB_STR -> {
                        var start = ireg[ins.extra()];
                        var end = ireg[ins.extra1()];
                        var str = objectToString(oreg[ins.src()]);

                        oreg[dst] = str.substring(Math.max(0, start), Math.min(str.length(), end));
                    }
                    case SCOMP -> {
                        ireg[dst] = compareStrings(oreg, ins.src(), ins.extra());
                    }
                    case TRAPS -> {
                        trapTableAddr = dst;
                        trapHandlerCount = ins.src();
                    }
                    case TRAP -> {
                        ip = trap(ireg[dst], registers, ip);
                    }
                }
            } catch (Throwable t) {
                ip = trap(t, registers, ip);
            }
        }
    }

    public String readString(int address) {
        if (address < 0) {
            return null;
        }
        int length = Math.min(memory.getInt(address), memory.capacity() / 2);
        char[] characters = new char[length];
        memory.slice(address, length * 2).asCharBuffer().get(characters);
        return new String(characters);
    }

    public String objectToString(Object obj) {
        return obj == null ? "null" : obj.toString();
    }

    public int trap(Throwable t, Registers registers, int ip){
        return trap(getTrapCode(t), registers, ip, t);
    }

    public int trap(int code, Registers registers, int ip){
        return trap(code, registers, ip, null);
    }

    public int trap(int code, Registers registers, int ip, Throwable t){
        var handler = getTrapHandler(code);
        if(handler != -1){
            // ip - 1 because when this is called ip has always
            // been advanced to the next instruction. It's simpler
            // to do this once here than subtract one at every trap
            // call site.
            registers.ireg()[0] = ip - 1;
            return handler;
        }else{
            throw new HummingbirdException(ip - 1, registers, t);
        }
    }

    public int getTrapCode(Throwable t){
        var code = -1;
        for(int i = 0; i < trapCodes.size(); i++){
            if(trapCodes.get(i).apply(t) > 0){
                return i;
            }
        }
        return code;
    }
    public int getTrapHandler(int exCode){
        // Trap table layout: sequence of integer trap codes,
        // followed by a sequence of trap handler addresses,
        // 1 address per code.
        if(trapTableAddr == -1 || exCode < 0){
            return -1;
        }
        var start = trapTableAddr;
        var end = trapTableAddr + trapHandlerCount;
        while(start <= end){
            var m = (start + end) / 2;
            var mCode = memory.getInt(m);
            if(mCode < exCode){
                start = m + 1;
            }else if(mCode > exCode){
                end = m - 1;
            }else{
                return memory.getInt(trapTableAddr + trapHandlerCount + m);
            }
        }
        return -1;
    }

    public static long longFromInts(int high, int low) {
        return ((long) high << 32) | ((long) low);
    }

    private static boolean condInts(int cond, Registers registers, int dst, int src) {
        switch (cond) {
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

    private static boolean condFloats(int cond, Registers registers, int dst, int src) {
        switch (cond) {
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

    private static boolean condLongs(int cond, Registers registers, int dst, int src) {
        switch (cond) {
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

    private static boolean condDoubles(int cond, Registers registers, int dst, int src) {
        switch (cond) {
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

    private static int compareStrings(Object[] oreg, int a, int b) {
        var objA = oreg[a];
        var objB = oreg[b];
        if (objA instanceof String strA && objB instanceof String strB) {
            return strA.compareTo(strB);
        }
        return Integer.MAX_VALUE;
    }

    private static boolean condObjects(int cond, Registers registers, int dst, int src) {
        switch (cond) {
            case COND_EQ -> {
                return registers.oreg()[dst] == registers.oreg()[src];
            }
            case COND_NULL -> {
                return registers.oreg()[dst] == null;
            }
        }
        return false;
    }

    private static int toInt(Registers registers, int src) {
        var type = Opcodes.registerType(src);
        src = Opcodes.registerIndex(src);
        return switch (type) {
            case INT_T -> registers.ireg()[src];
            case FLOAT_T -> (int) registers.freg()[src];
            case LONG_T -> (int) registers.lreg()[src];
            case DOUBLE_T -> (int) registers.dreg()[src];
            default -> 0;
        };
    }

    private static float toFloat(Registers registers, int src) {
        var type = Opcodes.registerType(src);
        src = Opcodes.registerIndex(src);
        return switch (type) {
            case INT_T -> (float) registers.ireg()[src];
            case FLOAT_T -> registers.freg()[src];
            case LONG_T -> (float) registers.lreg()[src];
            case DOUBLE_T -> (float) registers.dreg()[src];
            default -> Float.NaN;
        };
    }

    private static long toLong(Registers registers, int src) {
        var type = Opcodes.registerType(src);
        src = Opcodes.registerIndex(src);
        return switch (type) {
            case INT_T -> registers.ireg()[src];
            case FLOAT_T -> (long) registers.freg()[src];
            case LONG_T -> registers.lreg()[src];
            case DOUBLE_T -> (long) registers.dreg()[src];
            default -> 0;
        };
    }

    private static double toDouble(Registers registers, int src) {
        var type = Opcodes.registerType(src);
        src = Opcodes.registerIndex(src);
        return switch (type) {
            case INT_T -> (double) registers.ireg()[src];
            case FLOAT_T -> (double) registers.freg()[src];
            case LONG_T -> (double) registers.lreg()[src];
            case DOUBLE_T -> registers.dreg()[src];
            default -> Double.NaN;
        };
    }

    private static Registers allocateRegisters(int i, int f, int l, int d, int o) {
        return new Registers(
                new int[i],
                new float[f],
                new long[l],
                new double[d],
                new Object[o]
        );
    }

    public static void copyRegisters(Registers from, Registers to) {
        System.arraycopy(from.ireg(), 0, to.ireg(), 0, from.ireg().length);
        System.arraycopy(from.freg(), 0, to.freg(), 0, from.freg().length);
        System.arraycopy(from.lreg(), 0, to.lreg(), 0, from.lreg().length);
        System.arraycopy(from.dreg(), 0, to.dreg(), 0, from.dreg().length);
    }

}
