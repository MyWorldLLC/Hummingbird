package myworld.hummingbird;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;

import static myworld.hummingbird.Opcodes.*;

public final class HummingbirdVM {

    private final Executable exe;
    private final MemoryLimits limits;
    private ByteBuffer memory;
    public Object[] objMemory;
    private Fiber currentFiber;
    private int trapTableAddr = -1;
    private int trapHandlerCount = 0;
    private List<Function<Throwable, Integer>> trapCodes;
    private final Deque<Fiber> runQueue;
    private final ForeignFunction[] foreign;

    private DebugHandler debugHandler;

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

        debugHandler = (vm, fiber, staticValue, dynamicValue) -> {
            System.out.println("Debug: " + staticValue + " " + dynamicValue);
        };
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

        var rType = TypeFlag.LONG;
        if (exe.symbols().length > 0) {
            rType = exe.symbols()[0].rType();
        }

        return switch (rType) {
            case LONG -> registers[0];
            case OBJECT -> objMemory[(int)registers[0]];
            case VOID -> null;
            default -> null; // TODO - remove obsoleted type flags
        };
    }

    public Fiber spawn(Symbol entry, long[] initialState) {
        return spawn(entry != null ? entry.offset() : 0, initialState);
    }

    public Fiber spawn(int entry, long[] initialState) {
        var registers = allocateRegisters( // TODO - variable size register file
                2000
        );

        if (initialState != null) {
            copyRegisters(initialState, registers);
        }

        var fiber = new Fiber(this, exe, registers);
        fiber.saveCallContext(Integer.MAX_VALUE, 0, 0);
        fiber.saveCallContext(entry, 0, 0);

        runQueue.push(fiber);

        return fiber;
    }

    private Fiber nextFiber() {
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

        fiber.restoreCallContext();
        var ip = fiber.ip;

        var instructions = exe.code();
        while (ip < instructions.length) {
            var ins = instructions[ip];
            ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
            /*ip++;
            switch (ins.opcode()) {
                case CONST -> {
                    ip = ins.impl().apply(ins, reg, regOffset, ip);
                }
                case NULL -> {
                    objMemory[regOffset + ins.dst()] = null;
                }
                case ADD -> {
                    //add(ins, reg, regOffset);
                    ins.impl().apply(ins, reg, regOffset, ip);
                }
                case SUB -> {
                    sub(ins, reg, regOffset);
                }
                case MUL -> {
                    mul(ins, reg, regOffset);
                }
                case DIV -> {
                    try {
                        div(ins, reg, regOffset);
                    } catch (ArithmeticException ex) {
                        trap(Traps.DIV_BY_ZERO, registers, ip);
                    }
                }
                case REM -> {
                    try {
                        rem(ins, reg, regOffset);
                    } catch (ArithmeticException ex) {
                        trap(Traps.DIV_BY_ZERO, registers, ip);
                    }
                }
                case NEG -> {
                    neg(ins, reg, regOffset);
                }
                case POW -> {
                    pow(ins, reg, regOffset);
                }
                case BAND -> {
                    band(ins, reg, regOffset);
                }
                case BOR -> {
                    bor(ins, reg, regOffset);
                }
                case BXOR -> {
                    bxor(ins, reg, regOffset);
                }
                case BNOT -> {
                    bnot(ins, reg, regOffset);
                }
                case BLSHIFT -> {
                    blshift(ins, reg, regOffset);
                }
                case BSRSHIFT -> {
                    brshift(ins, reg, regOffset);
                }
                case BURSHIFT -> {
                    burshift(ins, reg, regOffset);
                }
                case L2D -> {
                    reg[regOffset + ins.dst()] = Double.doubleToLongBits((double) reg[regOffset + ins.src()]);
                }
                case D2L -> {
                    reg[regOffset + ins.dst()] = (long) Double.longBitsToDouble(reg[regOffset + ins.src()]);
                }
                case GOTO -> {
                    ip = ins.dst();
                }
                case JMP -> {
                    ip = (int) reg[regOffset + ins.dst()];
                }
                case ICOND -> {
                    ip = ins.impl().apply(ins, reg, regOffset, ip);
                    //if (condLongs(ins, reg, regOffset)) {
                    //    ip = ins.extra1();
                    //}
                }
                case DCOND -> {
                    if (condDoubles(ins, reg, regOffset)) {
                        ip = ins.extra1();
                    }
                }
                case PARAM -> {
                    reg[regOffset + ins.dst()] = reg[savedRegisters.callerRegisterOffset() + ins.src()];
                }
                case PARAMS -> {
                    for(int i = 0; i < ins.extra(); i++){
                        reg[regOffset + ins.dst() + i] =
                                reg[savedRegisters.callerRegisterOffset() + ins.src() + i];
                    }
                }
                case COPY -> {
                    reg[regOffset + ins.dst()] = reg[regOffset + ins.src()];
                }
                case SAVE -> {
                    savedRegisters.save(reg, regOffset + ins.dst(), ins.src());
                }
                case RESTORE -> {
                    savedRegisters.restore(reg, regOffset + ins.dst(), ins.src());
                }
                case IP -> {
                    reg[regOffset + ins.dst()] = ip;
                }
                case CALL -> {
                    var symbol = exe.symbols()[ins.src()];

                    var callerOffset = regOffset;
                    var callerParams = ins.extra();
                    savedRegisters.saveCallContext(ip, regOffset, ins.dst());

                    regOffset += symbol.registers();
                    for(int i = 0; i < ins.extra1(); i++){
                        reg[regOffset + i] = reg[callerOffset + callerParams + i];
                    }
                    ip = symbol.offset();
                }
                case DCALL -> {
                    var symbol = exe.symbols()[(int) reg[regOffset + ins.src()]];

                    var callerOffset = regOffset;
                    var callerParams = ins.extra();
                    savedRegisters.saveCallContext(ip, regOffset, ins.dst());

                    regOffset += symbol.registers();
                    for(int i = 0; i < ins.extra1(); i++){
                        reg[regOffset + i] = reg[callerOffset + callerParams + i];
                    }
                    ip = symbol.offset();
                }
                case FCALL -> {
                    var symbol = exe.symbols()[ins.dst()];
                    var func = foreign[symbol.offset()];

                    savedRegisters.saveCallContext(ip, regOffset, ins.dst());

                    try {
                        func.call(this, currentFiber);
                    } catch (Exception e) {
                        ip = trap(e, registers, ip);
                    }
                }
                case DFCALL -> {
                    var symbol = exe.symbols()[(int) reg[regOffset + ins.dst()]];
                    var func = foreign[symbol.offset()];

                    savedRegisters.saveCallContext(ip, regOffset, ins.dst());

                    try {
                        func.call(this, currentFiber);
                    } catch (Exception e) {
                        ip = trap(e, registers, ip);
                    }
                }
                case RETURN -> {
                    savedRegisters.restoreCallContext(callCtx);

                    var result = callCtx.returnDest;
                    var returnOffset = callCtx.registerOffset;

                    reg[returnOffset + result] = reg[regOffset + ins.dst()];

                    ip = callCtx.ip;
                    regOffset = returnOffset;
                }
                case SPAWN -> {
                    objMemory[(int) reg[regOffset + ins.dst()]] = spawn(ins.src(), registers);
                }
                case YIELD -> {
                    savedRegisters.saveCallContext(ip, regOffset, 0);
                    return;
                }
                case BLOCK -> {
                    currentFiber.setState(Fiber.State.BLOCKED);
                    savedRegisters.saveCallContext(ip, regOffset, 0);
                    return;
                }
                case UNBLOCK -> {
                    ((Fiber) objMemory[(int) reg[regOffset + ins.dst()]]).setState(Fiber.State.RUNNABLE);
                }*/
                    /*
                    case WRITE -> {
                        var wType = Opcodes.registerType(ins.src());
                        var src = Opcodes.registerIndex(ins.src());
                        var addr = ireg[ins.dst()];
                        switch (wType) {
                            case INT_T -> memory.putInt(addr, ireg[regOffset + src]);
                            case FLOAT_T -> memory.putFloat(addr, freg[regOffset + src]);
                            case LONG_T -> memory.putLong(addr, reg[regOffset + src]);
                            case DOUBLE_T -> memory.putDouble(addr, dreg[regOffset + src]);
                            case OBJECT_T -> objMemory[addr] = oreg[regOffset + src];
                        }
                    }
                    case READ -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = memory.getInt(ireg[regOffset + ins.src()]);
                            case FLOAT_T -> freg[regOffset + dst] = memory.getFloat(ireg[regOffset + ins.src()]);
                            case LONG_T -> reg[regOffset + dst] = memory.getLong(ireg[regOffset + ins.src()]);
                            case DOUBLE_T -> dreg[regOffset + dst] = memory.getDouble(ireg[regOffset + ins.src()]);
                            case OBJECT_T -> oreg[regOffset + dst] = objMemory[ireg[regOffset + ins.src()]];
                        }
                    }
                    case SWRITE -> {
                        var wType = Opcodes.registerType(ins.src());
                        var src = Opcodes.registerIndex(ins.src());
                        var addr = ireg[ins.dst()];
                        long value = switch (wType) {
                            case INT_T -> ireg[regOffset + src];
                            case LONG_T -> reg[regOffset + src];
                            default -> 0;
                        };
                        switch (ins.extra()) {
                            case BYTE_T -> memory.put(addr, (byte) value);
                            case CHAR_T -> memory.putChar(addr, (char) value);
                            case SHORT_T -> memory.putShort(addr, (short) value);
                        }
                    }
                    case SREAD -> {
                        var addr = ireg[regOffset + ins.src()];
                        int value = switch (ins.extra()) {
                            case BYTE_T -> memory.get(addr);
                            case CHAR_T -> memory.getChar(addr);
                            case SHORT_T -> memory.getShort(addr);
                            default -> 0;
                        };
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = value;
                            case LONG_T -> reg[regOffset + dst] = value;
                        }
                    }
                    case GWRITE -> {
                        var wType = Opcodes.registerType(ins.src());
                        var src = Opcodes.registerIndex(ins.src());
                        var addr = ireg[regOffset + ins.dst()];
                        memory.put(ireg[regOffset + ins.extra()], (byte) ins.extra1());
                        switch (wType) {
                            case INT_T -> memory.putInt(addr, ireg[regOffset + src]);
                            case FLOAT_T -> memory.putFloat(addr, freg[regOffset + src]);
                            case LONG_T -> memory.putLong(addr, reg[regOffset + src]);
                            case DOUBLE_T -> memory.putDouble(addr, dreg[regOffset + src]);
                            case OBJECT_T -> objMemory[addr] = oreg[regOffset + src];
                        }
                    }
                    case GREAD -> {
                        var guard = memory.get(ireg[regOffset + ins.extra()]);
                        if (guard != 0) {
                            ip = ins.extra1();
                        }
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = memory.getInt(ireg[regOffset + ins.src()]);
                            case FLOAT_T -> freg[regOffset + dst] = memory.getFloat(ireg[regOffset + ins.src()]);
                            case LONG_T -> reg[regOffset + dst] = memory.getLong(ireg[regOffset + ins.src()]);
                            case DOUBLE_T -> dreg[regOffset + dst] = memory.getDouble(ireg[regOffset + ins.src()]);
                            case OBJECT_T -> oreg[regOffset + dst] = objMemory[ireg[regOffset + ins.src()]];
                        }
                    }
                    case MEM_COPY -> {
                        var start = ireg[regOffset + ins.src()];
                        var end = ireg[regOffset + ins.extra()];
                        memory.put(ireg[regOffset + dst], memory.slice(start, end), 0, end - start);
                    }
                    case OBJ_COPY -> {
                        var start = ireg[regOffset + ins.src()];
                        var end = ireg[regOffset + ins.extra()];
                        System.arraycopy(objMemory, start, objMemory, ireg[regOffset + dst], end - start);
                    }
                    case ALLOCATED -> {
                        switch (ins.src()) {
                            case OBJECT_T -> ireg[regOffset + dst] = objMemory.length;
                            default -> ireg[regOffset + dst] = memory.capacity();
                        }
                    }
                    case RESIZE -> {
                        var size = Math.min(ireg[regOffset + ins.dst()], limits.bytes());
                        var next = ByteBuffer.allocate(size);
                        next.put(0, memory, 0, Math.min(size, memory.capacity()));
                        memory = next;
                    }
                    case OBJ_RESIZE -> {
                        var size = Math.min(ireg[regOffset + ins.dst()], limits.objects());
                        var next = new Object[size];
                        System.arraycopy(objMemory, 0, next, 0, Math.min(size, objMemory.length));
                        objMemory = next;
                    }
                    case STR -> {
                        var sType = Opcodes.registerType(ins.src());
                        var src = Opcodes.registerIndex(ins.src());
                        switch (sType) {
                            case INT_T -> oreg[regOffset + dst] = Integer.toString(ireg[regOffset + src]);
                            case FLOAT_T -> oreg[regOffset + dst] = Float.toString(freg[regOffset + src]);
                            case LONG_T -> oreg[regOffset + dst] = Long.toString(reg[regOffset + src]);
                            case DOUBLE_T -> oreg[regOffset + dst] = Double.toString(dreg[regOffset + src]);
                            case OBJECT_T -> oreg[regOffset + dst] = objectToString(oreg[regOffset + src]);
                        }
                    }
                    case STR_LEN -> {
                        if (oreg[regOffset + ins.src()] instanceof String s) {
                            ireg[regOffset + dst] = s.length();
                        } else {
                            ireg[regOffset + dst] = 0;
                        }
                    }
                    case CHAR_AT -> {
                        if (oreg[regOffset + ins.src()] instanceof String s) {
                            ireg[regOffset + dst] = s.charAt(ireg[regOffset + ins.extra()]);
                        } else {
                            ireg[regOffset + dst] = 0;
                        }
                    }
                    case TO_CHARS -> {
                        var charBuf = memory.asCharBuffer();
                        var address = ireg[regOffset + dst];
                        if (oreg[regOffset + ins.src()] instanceof String s) {
                            var chars = s.toCharArray();
                            memory.putInt(address, chars.length);
                            charBuf.put((address + 4) / 2, chars);
                        }
                    }
                    case FROM_CHARS -> {
                        var address = ireg[regOffset + ins.src()];
                        oreg[regOffset + dst] = readString(address);
                    }
                    case CONCAT -> {
                        var a = oreg[regOffset + ins.src()];
                        var b = oreg[regOffset + ins.extra()];
                        oreg[regOffset + dst] = objectToString(a) + objectToString(b);
                    }
                    case SUB_STR -> {
                        var start = ireg[regOffset + ins.extra()];
                        var end = ireg[regOffset + ins.extra1()];
                        var str = objectToString(oreg[regOffset + ins.src()]);

                        oreg[regOffset + dst] = str.substring(Math.max(0, start), Math.min(str.length(), end));
                    }
                    case SCOMP -> {
                        ireg[regOffset + dst] = compareStrings(oreg, regOffset + ins.src(), regOffset + ins.extra());
                    }
                    case TRAPS -> {
                        trapTableAddr = dst;
                        trapHandlerCount = ins.src();
                    }
                    case TRAP -> {
                        ip = trap(ireg[regOffset + dst], registers, ip);
                    }*/
                    /*case DEBUG -> {
                        if(debugHandler != null){
                            debugHandler.debug(this, currentFiber, ins.dst(), reg[regOffset + ins.src()]);
                        }
                    }
            }*/
        }
    }

    private static void add(Opcode ins, long[] reg, int regOffset) {
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] + reg[regOffset + ins.extra()];
    }

    private static void sub(Opcode ins, long[] reg, int regOffset) {
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] - reg[regOffset + ins.extra()];
    }

    private static void mul(Opcode ins, long[] reg, int regOffset) {
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] * reg[regOffset + ins.extra()];
    }

    private static void div(Opcode ins, long[] reg, int regOffset) {
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] * reg[regOffset + ins.extra()];
    }

    private static void rem(Opcode ins, long[] reg, int regOffset) {
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] % reg[regOffset + ins.extra()];
    }

    private static void neg(Opcode ins, long[] reg, int regOffset) {
        reg[regOffset + ins.dst()] = -reg[regOffset + ins.src()];
    }

    private static void pow(Opcode ins, long[] reg, int regOffset) {
        reg[regOffset + ins.dst()] = (long) Math.pow(reg[regOffset + ins.src()], reg[regOffset + ins.extra()]);
    }

    private static void band(Opcode ins, long[] reg, int regOffset) {
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] & reg[regOffset + ins.extra()];
    }

    private static void bor(Opcode ins, long[] reg, int regOffset) {
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] | reg[regOffset + ins.extra()];
    }

    private static void bxor(Opcode ins, long[] reg, int regOffset) {
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] ^ reg[regOffset + ins.extra()];
    }

    private static void bnot(Opcode ins, long[] reg, int regOffset) {
        reg[regOffset + ins.dst()] = ~reg[regOffset + ins.src()];
    }

    private static void blshift(Opcode ins, long[] reg, int regOffset) {
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] << reg[regOffset + ins.extra()];
    }

    private static void brshift(Opcode ins, long[] reg, int regOffset) {
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] >> reg[regOffset + ins.extra()];
    }

    private static void burshift(Opcode ins, long[] reg, int regOffset) {
        reg[regOffset + ins.dst()] = reg[regOffset + ins.src()] >>> reg[regOffset + ins.extra()];
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

    public int trap(Throwable t, long[] registers, int ip) {
        return trap(getTrapCode(t), registers, ip, t);
    }

    public int trap(int code, long[] registers, int ip) {
        return trap(code, registers, ip, null);
    }

    public int trap(int code, long[] registers, int ip, Throwable t) {
        var handler = getTrapHandler(code);
        if (handler != -1) {
            // ip - 1 because when this is called ip has always
            // been advanced to the next instruction. It's simpler
            // to do this once here than subtract one at every trap
            // call site.
            registers[0] = ip - 1;
            return handler;
        } else {
            throw new HummingbirdException(ip - 1, registers, t);
        }
    }

    public int getTrapCode(Throwable t) {
        var code = -1;
        for (int i = 0; i < trapCodes.size(); i++) {
            if (trapCodes.get(i).apply(t) > 0) {
                return i;
            }
        }
        return code;
    }

    public int getTrapHandler(int exCode) {
        // Trap table layout: sequence of integer trap codes,
        // followed by a sequence of trap handler addresses,
        // 1 address per code.
        if (trapTableAddr == -1 || exCode < 0) {
            return -1;
        }
        var start = trapTableAddr;
        var end = trapTableAddr + trapHandlerCount;
        while (start <= end) {
            var m = (start + end) / 2;
            var mCode = memory.getInt(m);
            if (mCode < exCode) {
                start = m + 1;
            } else if (mCode > exCode) {
                end = m - 1;
            } else {
                return memory.getInt(trapTableAddr + trapHandlerCount + m);
            }
        }
        return -1;
    }

    public static long longFromInts(int high, int low) {
        return ((long) high << 32) | ((long) low);
    }

    private static boolean condLongs(Opcode ins, long[] reg, int regOffset) {
        var dst = regOffset + ins.dst();
        var src = regOffset + ins.src();
        switch (ins.extra()) {
            case COND_LT -> {
                return reg[dst] < reg[src];
            }
            case COND_LE -> {
                return reg[dst] <= reg[src];
            }
            case COND_EQ -> {
                return reg[dst] == reg[src];
            }
            case COND_GE -> {
                return reg[dst] >= reg[src];
            }
            case COND_GT -> {
                return reg[dst] > reg[src];
            }
        }
        return false;
    }

    private static boolean condDoubles(Opcode ins, long[] reg, int regOffset) {
        var dst = regOffset + ins.dst();
        var src = regOffset + ins.src();
        var a = Double.longBitsToDouble(reg[dst]);
        var b = Double.longBitsToDouble(reg[src]);
        switch (ins.extra()) {
            case COND_LT -> {
                return a < b;
            }
            case COND_LE -> {
                return a <= b;
            }
            case COND_EQ -> {
                return a == b;
            }
            case COND_GE -> {
                return a >= b;
            }
            case COND_GT -> {
                return a > b;
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

    private static boolean condObjects(int cond, long[] registers, int dst, int src) {
        switch (cond) {
            case COND_EQ -> {
                //return registers.oreg()[dst] == registers.oreg()[src];
            }
            case COND_NULL -> {
                //return registers.oreg()[dst] == null;
            }
        }
        return false;
    }

    private static long[] allocateRegisters(int l) {
        return new long[l];
    }

    public static void copyRegisters(long[] from, long[] to) {
        System.arraycopy(from, 0, to, 0, from.length);
    }

}
