package myworld.hummingbird;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;

public final class HummingbirdVM {

    private final Executable exe;
    public final MemoryLimits limits;
    public ByteBuffer memory;
    public Object[] objMemory;
    private Fiber currentFiber;
    private List<Function<Throwable, Integer>> trapCodes;
    private final Deque<Fiber> runQueue;
    public final ForeignFunction[] foreign;

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

        debugHandler = (fiber, staticValue, dynamicValue) -> {
            System.out.println("Debug: " + staticValue + " " + dynamicValue);
        };
    }

    public Symbol findFunction(String name, TypeFlag rType){
        for(var symbol : exe.symbols()){
            if(symbol.name().equals(name) && symbol.type().equals(Symbol.Type.FUNCTION)){
                if(symbol.rType().equals(rType)){
                    return symbol;
                }
            }
        }
        return null;
    }

    public Symbol findData(String name){
        for(var symbol : exe.symbols()){
            if(symbol.name().equals(name) && symbol.type().equals(Symbol.Type.DATA)){
                return symbol;
            }
        }
        return null;
    }

    public Symbol findForeignFunction(String name, TypeFlag rType){
        for(var symbol : exe.symbols()){
            if(symbol.name().equals(name) && symbol.type().equals(Symbol.Type.FOREIGN)){
                if(symbol.rType().equals(rType)){
                    return symbol;
                }
            }
        }
        return null;
    }

    public Object run(){
        return run(null, null);
    }

    public Object run(String name){
        return run(name, null);
    }

    public Object run(String name, TypeFlag rType) {

        var symbol = findForeignFunction(name, rType);

        spawn(symbol, null);

        currentFiber = nextFiber();
        var registers = currentFiber.registers;
        while (currentFiber != null) {
            registers = currentFiber.registers;
            run(currentFiber);
            currentFiber = nextFiber();
        }

        if(rType == null){
            rType = TypeFlag.LONG;
            if (exe.symbols().length > 0) {
                rType = exe.symbols()[0].rType();
            }
        }

        return switch (rType) {
            case INT -> (int) registers[0];
            case FLOAT -> (float) Double.longBitsToDouble(registers[0]);
            case LONG -> registers[0];
            case DOUBLE -> Double.longBitsToDouble(registers[0]);
            case OBJECT -> objMemory[(int)registers[0]];
            case STRING -> objectToString((int)registers[0]);
            case VOID -> null;
        };
    }

    public Fiber spawn(Symbol entry, long[] initialState) {
        return spawn(entry != null ? entry.offset() : 0, initialState);
    }

    public Fiber spawn(int ip, long[] initialState) {
        var registers = allocateRegisters( // TODO - variable size register file
                2000
        );

        if (initialState != null) {
            copyRegisters(initialState, registers);
        }

        var fiber = new Fiber(this, exe, registers);
        fiber.saveCallContext(Integer.MAX_VALUE, 0, 0);
        fiber.saveCallContext(ip, Fiber.CALL_FRAME_SAVED_REGISTERS, 0);

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

    public void enqueue(Fiber fiber){
        runQueue.add(fiber);
    }

    /**
     * Dispatch loop uses the "Nostradamus Distributor" pattern. The technical motivation and explanation for this
     * pattern is available at:
     * <a href="http://www.emulators.com/docs/nx25_nostradamus.htm">...</a>
     * <p>
     * In some benchmarks this will not have much (if any) impact, but in others the speedup is dramatic. The highest
     * measured speedup was a factor of ~3x better than without it. This is supported by the assembler via the "hot loop label"
     * syntax (prefixing a label target with `$$` instead of just `$`, which the assembler resolves as a negative, signaling
     * to the VM to return control flow to the top of the dispatch loop. This allows the interpreter's dispatch loop
     * to mirror (from the perspective of the CPU branch predictor) the HVM instructions, allowing the branch predictor
     * to correctly predict which HVM instruction will be dispatched next.
     */
    public void run(Fiber fiber) throws HummingbirdException {

        fiber.restoreCallContext();
        var ip = fiber.ip;

        var instructions = exe.code();
        while (Math.abs(ip) < instructions.length) {
            try {
                ip = Math.abs(ip);
                var ins = instructions[ip];
                ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                if (ip >= 0) {
                    ins = instructions[ip];
                    ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                    if (ip >= 0) {
                        ins = instructions[ip];
                        ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                        if (ip >= 0) {
                            ins = instructions[ip];
                            ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                            if (ip >= 0) {
                                ins = instructions[ip];
                                ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                if (ip >= 0) {
                                    ins = instructions[ip];
                                    ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                    if (ip >= 0) {
                                        ins = instructions[ip];
                                        ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                        if (ip >= 0) {
                                            ins = instructions[ip];
                                            ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                            if (ip >= 0) {
                                                ins = instructions[ip];
                                                ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                if (ip >= 0) {
                                                    ins = instructions[ip];
                                                    ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                    if (ip >= 0) {
                                                        ins = instructions[ip];
                                                        ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                        if (ip >= 0) {
                                                            ins = instructions[ip];
                                                            ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                            if (ip >= 0) {
                                                                ins = instructions[ip];
                                                                ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                if (ip >= 0) {
                                                                    ins = instructions[ip];
                                                                    ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                    if (ip >= 0) {
                                                                        ins = instructions[ip];
                                                                        ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                        if (ip >= 0) {
                                                                            ins = instructions[ip];
                                                                            ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                            if (ip >= 0) {
                                                                                ins = instructions[ip];
                                                                                ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                if (ip >= 0) {
                                                                                    ins = instructions[ip];
                                                                                    ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                    if (ip >= 0) {
                                                                                        ins = instructions[ip];
                                                                                        ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                        if (ip >= 0) {
                                                                                            ins = instructions[ip];
                                                                                            ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                            if (ip >= 0) {
                                                                                                ins = instructions[ip];
                                                                                                ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                if (ip >= 0) {
                                                                                                    ins = instructions[ip];
                                                                                                    ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                    if (ip >= 0) {
                                                                                                        ins = instructions[ip];
                                                                                                        ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                        if (ip >= 0) {
                                                                                                            ins = instructions[ip];
                                                                                                            ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                            if (ip >= 0) {
                                                                                                                ins = instructions[ip];
                                                                                                                ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                if (ip >= 0) {
                                                                                                                    ins = instructions[ip];
                                                                                                                    ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                    if (ip >= 0) {
                                                                                                                        ins = instructions[ip];
                                                                                                                        ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                        if (ip >= 0) {
                                                                                                                            ins = instructions[ip];
                                                                                                                            ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                            if (ip >= 0) {
                                                                                                                                ins = instructions[ip];
                                                                                                                                ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                if (ip >= 0) {
                                                                                                                                    ins = instructions[ip];
                                                                                                                                    ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                    if (ip >= 0) {
                                                                                                                                        ins = instructions[ip];
                                                                                                                                        ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                        if (ip >= 0) {
                                                                                                                                            ins = instructions[ip];
                                                                                                                                            ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                            if (ip >= 0) {
                                                                                                                                                ins = instructions[ip];
                                                                                                                                                ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                if (ip >= 0) {
                                                                                                                                                    ins = instructions[ip];
                                                                                                                                                    ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                    if (ip >= 0) {
                                                                                                                                                        ins = instructions[ip];
                                                                                                                                                        ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                        if (ip >= 0) {
                                                                                                                                                            ins = instructions[ip];
                                                                                                                                                            ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                            if (ip >= 0) {
                                                                                                                                                                ins = instructions[ip];
                                                                                                                                                                ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                                if (ip >= 0) {
                                                                                                                                                                    ins = instructions[ip];
                                                                                                                                                                    ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                                    if (ip >= 0) {
                                                                                                                                                                        ins = instructions[ip];
                                                                                                                                                                        ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                                        if (ip >= 0) {
                                                                                                                                                                            ins = instructions[ip];
                                                                                                                                                                            ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                                            if (ip >= 0) {
                                                                                                                                                                                ins = instructions[ip];
                                                                                                                                                                                ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                                                if (ip >= 0) {
                                                                                                                                                                                    ins = instructions[ip];
                                                                                                                                                                                    ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                                                    if (ip >= 0) {
                                                                                                                                                                                        ins = instructions[ip];
                                                                                                                                                                                        ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                                                        if (ip >= 0) {
                                                                                                                                                                                            ins = instructions[ip];
                                                                                                                                                                                            ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                                                            if (ip >= 0) {
                                                                                                                                                                                                ins = instructions[ip];
                                                                                                                                                                                                ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                                                                if (ip >= 0) {
                                                                                                                                                                                                    ins = instructions[ip];
                                                                                                                                                                                                    ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                                                                    if (ip >= 0) {
                                                                                                                                                                                                        ins = instructions[ip];
                                                                                                                                                                                                        ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                                                                        if (ip >= 0) {
                                                                                                                                                                                                            ins = instructions[ip];
                                                                                                                                                                                                            ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                                                                            if (ip >= 0) {
                                                                                                                                                                                                                ins = instructions[ip];
                                                                                                                                                                                                                ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                                                                                if (ip >= 0) {
                                                                                                                                                                                                                    ins = instructions[ip];
                                                                                                                                                                                                                    ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                                                                                    if (ip >= 0) {
                                                                                                                                                                                                                        ins = instructions[ip];
                                                                                                                                                                                                                        ip = ins.impl().apply(fiber, ins, fiber.registerOffset, ip, instructions);
                                                                                                                                                                                                                    }
                                                                                                                                                                                                                }
                                                                                                                                                                                                            }
                                                                                                                                                                                                        }
                                                                                                                                                                                                    }
                                                                                                                                                                                                }
                                                                                                                                                                                            }
                                                                                                                                                                                        }
                                                                                                                                                                                    }
                                                                                                                                                                                }
                                                                                                                                                                            }
                                                                                                                                                                        }
                                                                                                                                                                    }
                                                                                                                                                                }
                                                                                                                                                            }
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }catch (HummingbirdException e){
                throw e;
            }catch (Throwable t){
                fiber.vm.trap(t, fiber.registers, ip);
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

    public String objectToString(int address){
        return objectToString(objMemory[address]);
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
            // TODO - Invoke trap handler like a normal function, passing the ip as a parameter
            // without stomping R0.
            registers[0] = ip;
            return handler;
        } else {
            throw new HummingbirdException(ip, registers, t);
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

        var trapTableAddr = currentFiber.trapTableAddr;
        var trapHandlerCount = currentFiber.trapHandlerCount;

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

    public int resize(int newSize){
        var size = Math.min(newSize, limits.bytes());

        var next = ByteBuffer.allocate(size);
        next.put(0, memory, 0, Math.min(size, memory.capacity()));
        memory = next;

        return size;
    }

    public int resizeObj(int newSize){
        var size = Math.min(newSize, limits.objects());

        var next = new Object[size];
        System.arraycopy(objMemory, 0, next, 0, Math.min(size, objMemory.length));
        objMemory = next;

        return size;
    }

    public DebugHandler getDebugHandler(){
        return debugHandler;
    }

    public static long longFromInts(int high, int low) {
        return ((long) high << 32) | ((long) low);
    }

    private static long[] allocateRegisters(int l) {
        return new long[l];
    }

    public static void copyRegisters(long[] from, long[] to) {
        System.arraycopy(from, 0, to, 0, from.length);
    }

}
