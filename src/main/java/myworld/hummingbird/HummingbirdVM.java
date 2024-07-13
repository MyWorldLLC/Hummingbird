package myworld.hummingbird;

import static myworld.hummingbird.CoreOpcodes.*;

public class HummingbirdVM {

    protected final Executable exe;

    public HummingbirdVM(Executable exe){
        this.exe = exe;
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
                    registers.ireg()[ins.dst()] += registers.ireg()[ins.src()];
                    ip++;
                }
                case IFLT -> {
                    if(registers.ireg()[ins.dst()] < registers.ireg()[ins.src()]){
                        ip = ins.extra();
                    }else{
                        stop = true;
                    }
                }
                case CONST -> {
                    registers.ireg()[ins.dst()] = ins.src();
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

}
