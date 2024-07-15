package myworld.hummingbird;

import java.nio.ByteBuffer;
import java.util.Arrays;

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
                ip++;
                switch (ins.opcode()) {
                    case I_CONST -> {
                        registers.ireg()[ins.dst()] = ins.src();
                    }
                    case F_CONST -> {
                        registers.freg()[ins.dst()] = Float.intBitsToFloat(ins.src());
                    }case L_CONST -> {
                        registers.lreg()[ins.dst()] = longFromInts(ins.src(), ins.extra());
                    }
                    case D_CONST -> {
                        registers.dreg()[ins.dst()] = Double.longBitsToDouble(longFromInts(ins.src(), ins.extra()));
                    }
                    case S_CONST -> {
                        registers.sreg()[ins.dst()] = constString(ins.src());
                    }
                    case I_ADD -> {
                        registers.ireg()[ins.dst()] += registers.ireg()[ins.src()];
                    }
                    case I_IFLT -> {
                        if (registers.ireg()[ins.dst()] < registers.ireg()[ins.src()]) {
                            ip = ins.extra();
                        } else {
                            stop = true;
                        }
                    }
                    case I_RETURN -> {
                        // TODO - unwind VM call stack and place result in call target register
                        return registers.ireg()[0];
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
        int length = Math.min(memory.getInt(address), memory.capacity() / 2);
        char[] characters = new char[length];
        memory.slice(address, length * 2).asCharBuffer().get(characters);
        return new String(characters);
    }

    public static long longFromInts(int high, int low) {
        return ((long) high << 32) | ((long) low);
    }

    public static final int I_CONST = 0;
    public static final int F_CONST = 1;
    public static final int L_CONST = 2;
    public static final int D_CONST = 3;
    public static final int S_CONST = 4;

    public static final int I_ADD = 5;

    public static final int I_IFLT = 6;
    public static final int I_RETURN = 7;

}
