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
        var registers = allocateRegisters( // TODO - variable size register file
                2000,
                2000,
                2000,
                2000,
                2000
        );

        if (initialState != null) {
            copyRegisters(initialState, registers);
        }

        var savedRegisters = new SavedRegisters(1000);
        savedRegisters.saveIp(Integer.MAX_VALUE);
        savedRegisters.saveRegisterOffset(0);
        var fiber = new Fiber(registers, savedRegisters);

        savedRegisters.saveIp(entry);
        savedRegisters.saveIp(0); // Return location

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
        var regOffset = 0;

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
                            case INT_T -> ireg[regOffset + dst] = ins.src();
                            case FLOAT_T -> freg[regOffset + dst] = Float.intBitsToFloat(ins.src());
                            case LONG_T -> lreg[regOffset + dst] = longFromInts(ins.src(), ins.extra());
                            case DOUBLE_T -> dreg[regOffset + dst] = Double.longBitsToDouble(longFromInts(ins.src(), ins.extra()));
                        }
                    }
                    case NULL -> {
                        oreg[regOffset + dst] = null;
                    }
                    case ADD -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = ireg[regOffset + ins.src()] + ireg[regOffset + ins.extra()];
                            case FLOAT_T -> freg[regOffset + dst] = freg[regOffset + ins.src()] + freg[regOffset + ins.extra()];
                            case LONG_T -> lreg[regOffset + dst] = lreg[regOffset + ins.src()] + lreg[regOffset + ins.extra()];
                            case DOUBLE_T -> dreg[regOffset + dst] = dreg[regOffset + ins.src()] + dreg[regOffset + ins.extra()];
                        }
                    }
                    case SUB -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = ireg[regOffset + ins.src()] - ireg[regOffset + ins.extra()];
                            case FLOAT_T -> freg[regOffset + dst] = freg[regOffset + ins.src()] - freg[regOffset + ins.extra()];
                            case LONG_T -> lreg[regOffset + dst] = lreg[regOffset + ins.src()] - lreg[regOffset + ins.extra()];
                            case DOUBLE_T -> dreg[regOffset + dst] = dreg[regOffset + ins.src()] - dreg[regOffset + ins.extra()];
                        }
                    }
                    case MUL -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = ireg[regOffset + ins.src()] * ireg[regOffset + ins.extra()];
                            case FLOAT_T -> freg[regOffset + dst] = freg[regOffset + ins.src()] * freg[regOffset + ins.extra()];
                            case LONG_T -> lreg[regOffset + dst] = lreg[regOffset + ins.src()] * lreg[regOffset + ins.extra()];
                            case DOUBLE_T -> dreg[regOffset + dst] = dreg[regOffset + ins.src()] * dreg[regOffset + ins.extra()];
                        }
                    }
                    case DIV -> {
                        try{
                            switch (type) {
                                case INT_T -> ireg[regOffset + dst] = ireg[regOffset + ins.src()] / ireg[regOffset + ins.extra()];
                                case FLOAT_T -> freg[regOffset + dst] = freg[regOffset + ins.src()] / freg[regOffset + ins.extra()];
                                case LONG_T -> lreg[regOffset + dst] = lreg[regOffset + ins.src()] / lreg[regOffset + ins.extra()];
                                case DOUBLE_T -> dreg[regOffset + dst] = dreg[regOffset + ins.src()] / dreg[regOffset + ins.extra()];
                            }
                        }catch(ArithmeticException ex){
                            trap(Traps.DIV_BY_ZERO, registers, ip);
                        }
                    }
                    case REM -> {
                        try{
                            switch (type) {
                                case INT_T -> ireg[regOffset + dst] = ireg[regOffset + ins.src()] % ireg[regOffset + ins.extra()];
                                case FLOAT_T -> freg[regOffset + dst] = freg[regOffset + ins.src()] % freg[regOffset + ins.extra()];
                                case LONG_T -> lreg[regOffset + dst] = lreg[regOffset + ins.src()] % lreg[regOffset + ins.extra()];
                                case DOUBLE_T -> dreg[regOffset + dst] = dreg[regOffset + ins.src()] % dreg[regOffset + ins.extra()];
                            }
                        }catch(ArithmeticException ex){
                            trap(Traps.DIV_BY_ZERO, registers, ip);
                        }
                    }
                    case NEG -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = -ireg[regOffset + ins.src()];
                            case FLOAT_T -> freg[regOffset + dst] = -freg[regOffset + ins.src()];
                            case LONG_T -> lreg[regOffset + dst] = -lreg[regOffset + ins.src()];
                            case DOUBLE_T -> dreg[regOffset + dst] = -dreg[regOffset + ins.src()];
                        }
                    }
                    case POW -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = (int) Math.pow(ireg[regOffset + ins.src()], ireg[regOffset + ins.extra()]);
                            case FLOAT_T -> freg[regOffset + dst] = (float) Math.pow(freg[regOffset + ins.src()], freg[regOffset + ins.extra()]);
                            case LONG_T -> lreg[regOffset + dst] = (long) Math.pow(lreg[regOffset + ins.src()], lreg[regOffset + ins.extra()]);
                            case DOUBLE_T -> dreg[regOffset + dst] = Math.pow(dreg[regOffset + ins.src()], dreg[regOffset + ins.extra()]);
                        }
                    }
                    case BAND -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = ireg[regOffset + ins.src()] & ireg[regOffset + ins.extra()];
                            case LONG_T -> lreg[regOffset + dst] = lreg[regOffset + ins.src()] & lreg[regOffset + ins.extra()];
                        }
                    }
                    case BOR -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = ireg[regOffset + ins.src()] | ireg[regOffset + ins.extra()];
                            case LONG_T -> lreg[regOffset + dst] = lreg[regOffset + ins.src()] | lreg[regOffset + ins.extra()];
                        }
                    }
                    case BXOR -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = ireg[regOffset + ins.src()] ^ ireg[regOffset + ins.extra()];
                            case LONG_T -> lreg[regOffset + dst] = lreg[regOffset + ins.src()] ^ lreg[regOffset + ins.extra()];
                        }
                    }
                    case BNOT -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = ~ireg[regOffset + ins.src()];
                            case LONG_T -> lreg[regOffset + dst] = ~lreg[regOffset + ins.src()];
                        }
                    }
                    case BLSHIFT -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = ireg[regOffset + ins.src()] << ireg[regOffset + ins.extra()];
                            case LONG_T -> lreg[regOffset + dst] = lreg[regOffset + ins.src()] << lreg[regOffset + ins.extra()];
                        }
                    }
                    case BSRSHIFT -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = ireg[regOffset + ins.src()] >> ireg[regOffset + ins.extra()];
                            case LONG_T -> lreg[regOffset + dst] = lreg[regOffset + ins.src()] >> lreg[regOffset + ins.extra()];
                        }
                    }
                    case BURSHIFT -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = ireg[regOffset + ins.src()] >>> ireg[regOffset + ins.extra()];
                            case LONG_T -> lreg[regOffset + dst] = lreg[regOffset + ins.src()] >>> lreg[regOffset + ins.extra()];
                        }
                    }
                    case CONV -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = toInt(registers, regOffset + ins.src());
                            case FLOAT_T -> freg[regOffset + dst] = toFloat(registers, regOffset + ins.src());
                            case LONG_T -> lreg[regOffset + dst] = toLong(registers, regOffset + ins.src());
                            case DOUBLE_T -> dreg[regOffset + dst] = toDouble(registers, regOffset + ins.src());
                        }
                    }
                    case GOTO -> {
                        ip = dst;
                    }
                    case JMP -> {
                        ip = ireg[regOffset + dst];
                    }
                    case ICOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if (condInts(cond, registers, regOffset + dst, regOffset + src)) {
                            ip = ins.extra();
                        }
                    }
                    case FCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if (condFloats(cond, registers, regOffset + dst, regOffset + src)) {
                            ip = ins.extra();
                        }
                    }
                    case LCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if (condLongs(cond, registers, regOffset + dst, regOffset + src)) {
                            ip = ins.extra();
                        }
                    }
                    case DCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if (condDoubles(cond, registers, regOffset + dst, regOffset + src)) {
                            ip = ins.extra();
                        }
                    }
                    case OCOND -> {
                        var src = Opcodes.registerIndex(ins.src());
                        var cond = Opcodes.registerType(ins.src());
                        if (condObjects(cond, registers, regOffset + dst, regOffset + src)) {
                            ip = ins.extra();
                        }
                    }
                    case RETURN -> {
                        var result = savedRegisters.restoreIp();
                        ip = savedRegisters.restoreIp();
                        var returnOffset = savedRegisters.restoreRegisterOffset();
                        switch (type){
                            case INT_T -> ireg[returnOffset + result] = ireg[regOffset + dst];
                            case FLOAT_T -> freg[returnOffset + result] = freg[regOffset + dst];
                            case LONG_T -> lreg[returnOffset + result] = lreg[regOffset + dst];
                            case DOUBLE_T -> dreg[returnOffset + result] = dreg[regOffset + dst];
                            case OBJECT_T -> oreg[returnOffset + result] = oreg[regOffset + dst];
                        }

                        regOffset = returnOffset;
                    }
                    case COPY -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = ireg[regOffset + ins.src()];
                            case FLOAT_T -> freg[regOffset + dst] = freg[regOffset + ins.src()];
                            case LONG_T -> lreg[regOffset + dst] = lreg[regOffset + ins.src()];
                            case DOUBLE_T -> dreg[regOffset + dst] = dreg[regOffset + ins.src()];
                            case OBJECT_T -> oreg[regOffset + dst] = oreg[regOffset + ins.src()];
                        }
                    }
                    case SAVE -> {
                        switch (type) {
                            case INT_T -> savedRegisters.save(ireg, regOffset + dst, regOffset + ins.src());
                            case FLOAT_T -> savedRegisters.save(freg, regOffset + dst, regOffset + ins.src());
                            case LONG_T -> savedRegisters.save(lreg, regOffset + dst, regOffset + ins.src());
                            case DOUBLE_T -> savedRegisters.save(dreg, regOffset + dst, regOffset + ins.src());
                            case OBJECT_T -> savedRegisters.save(oreg, regOffset + dst, regOffset + ins.src());
                        }
                    }
                    case RESTORE -> {
                        switch (type) {
                            case INT_T -> savedRegisters.restore(ireg, regOffset + dst, ins.src());
                            case FLOAT_T -> savedRegisters.restore(freg, regOffset + dst, ins.src());
                            case LONG_T -> savedRegisters.restore(lreg, regOffset + dst, ins.src());
                            case DOUBLE_T -> savedRegisters.restore(dreg, regOffset + dst, ins.src());
                            case OBJECT_T -> savedRegisters.restore(oreg, regOffset + dst, ins.src());
                        }
                    }
                    case IP -> {
                        ireg[dst] = ip;
                    }
                    case CALL -> {
                        var symbol = exe.symbols()[ins.src()];

                        savedRegisters.saveIp(ip);
                        savedRegisters.saveIp(dst);
                        savedRegisters.saveRegisterOffset(regOffset);
                        regOffset += 6; // TODO - get this from symbol

                        ip = symbol.offset();
                    }
                    case DCALL -> {
                        savedRegisters.saveIp(ip);
                        ip = ireg[regOffset + dst];
                    }
                    case FCALL -> {
                        var symbol = exe.symbols()[dst];
                        var func = foreign[symbol.offset()];
                        func.call(this, currentFiber);
                    }
                    case DFCALL -> {
                        var symbol = exe.symbols()[ireg[regOffset + dst]];
                        var func = foreign[symbol.offset()];
                        func.call(this, currentFiber);
                    }
                    case SPAWN -> {
                        oreg[regOffset + dst] = spawn(ins.src(), registers);
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
                            case INT_T -> memory.putInt(addr, ireg[regOffset + src]);
                            case FLOAT_T -> memory.putFloat(addr, freg[regOffset + src]);
                            case LONG_T -> memory.putLong(addr, lreg[regOffset + src]);
                            case DOUBLE_T -> memory.putDouble(addr, dreg[regOffset + src]);
                            case OBJECT_T -> objMemory[addr] = oreg[regOffset + src];
                        }
                    }
                    case READ -> {
                        switch (type) {
                            case INT_T -> ireg[regOffset + dst] = memory.getInt(ireg[regOffset + ins.src()]);
                            case FLOAT_T -> freg[regOffset + dst] = memory.getFloat(ireg[regOffset + ins.src()]);
                            case LONG_T -> lreg[regOffset + dst] = memory.getLong(ireg[regOffset + ins.src()]);
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
                            case LONG_T -> lreg[regOffset + src];
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
                            case LONG_T -> lreg[regOffset + dst] = value;
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
                            case LONG_T -> memory.putLong(addr, lreg[regOffset + src]);
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
                            case LONG_T -> lreg[regOffset + dst] = memory.getLong(ireg[regOffset + ins.src()]);
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
                            case LONG_T -> oreg[regOffset + dst] = Long.toString(lreg[regOffset + src]);
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
                    }
                    case PARAM -> {
                        ireg[regOffset + dst] = ireg[savedRegisters.callerRegisterOffset() + ins.src()];
                    }
                    case DEBUG -> {
                        System.out.println("Debug @" + ip + ": " + ins.dst() + " " + ireg[regOffset + ins.src()]);
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
