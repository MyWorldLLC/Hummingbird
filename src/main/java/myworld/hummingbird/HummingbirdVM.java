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
                    /*
                    case SCOMP -> {
                        ireg[regOffset + dst] = compareStrings(oreg, regOffset + ins.src(), regOffset + ins.extra());
                    }*/

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
            // TODO - instead of current mechanism, spawn a fiber at the trap handler with
            // the trap site and trapped fiber as parameters. Need to work out passing objects
            // since we no longer have object registers (may want to bring back object registers).
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
