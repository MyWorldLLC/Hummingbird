package myworld.hummingbird.test;

import myworld.hummingbird.HummingbirdVM;
import myworld.hummingbird.assembler.Assembler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import static myworld.hummingbird.test.TestPrograms.DCOp.*;

public class TestPrograms {

    public final HummingbirdVM countOneMillion = load("myworld/hummingbird/test/programs/countOneMillion.hasm");
    public final HummingbirdVM callOneMillion = load("myworld/hummingbird/test/programs/callOneMillion.hasm");
    public final HummingbirdVM fibonacci30 = load("myworld/hummingbird/test/programs/fibonacci30.hasm");
    public final HummingbirdVM goldenRatio = load("myworld/hummingbird/test/programs/goldenRatio.hasm");
    public final HummingbirdVM simpleFunction = load("myworld/hummingbird/test/programs/simpleFunction.hasm");
    public final HummingbirdVM simpleFiber = load("myworld/hummingbird/test/programs/simpleFiber.hasm");
    public final HummingbirdVM recursiveCountOneMillion = load("myworld/hummingbird/test/programs/recursiveCountOneMillion.hasm");
    public final HummingbirdVM mathBench = load("myworld/hummingbird/test/programs/mathBench.hasm");

    public Map<String, Callable<Object>> javaTestPrograms(){
        var programs = new HashMap<String, Callable<Object>>();

        programs.put("countOneMillion", this::countOneMillion);
        programs.put("returnConstant", this::returnConstant);
        programs.put("decodeChainedDispatch", decodeChainedDispatch());

        return programs;
    }

    public Map<String, HummingbirdVM> hvmTestPrograms(){

        var programs = new HashMap<String, HummingbirdVM>();

        programs.put("countOneMillion", countOneMillion);
        programs.put("callOneMillion", callOneMillion);
        programs.put("fibonacci30", fibonacci30);
        programs.put("simpleFiber", simpleFiber);
        programs.put("simpleFunction", simpleFunction);
        programs.put("recursiveCountOneMillion", recursiveCountOneMillion);
        programs.put("goldenRatio", goldenRatio);
        programs.put("mathBench", mathBench);

        return programs;
    }

    public HummingbirdVM load(String program){

        try{
            var is = getClass().getClassLoader().getResourceAsStream(program);
            Objects.requireNonNull(is, "Test program " + program + " not found");

            var reader = new BufferedReader(new InputStreamReader(is));

            var builder = new StringBuilder();
            var line = reader.readLine();
            while(line != null){
                builder.append(line);
                builder.append('\n');
                line = reader.readLine();
            }

            reader.close();

            var assembler = new Assembler();
            var exe = assembler.assemble(builder);

            return new HummingbirdVM(exe);

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public int countOneMillion(){
        int x = 0;
        while(x < 1000000) {
            x = x + 1;
        }
        return x;
    }

    public int returnConstant(){
        return 1000;
    }

    private interface DCOpImpl {

        int exec(DCOp[] program, int[] registers, int ip);

    }

    private class IFLT implements DCOpImpl {

        @Override
        public int exec(DCOp[] program, int[] registers, int ip) {
            var op = program[ip];
            var t1 = decodeOp(op.immediates, DST_MASK, op.dst, registers);
            var t2 = decodeOp(op.immediates, SRC_MASK, op.src, registers);
            if(t1 < t2){
                ip = decodeOp(op.immediates, OP_MASK, op.op1, registers);
            }else{
                ip++;
            }
            return ip;
        }
    }

    private class ADD implements DCOpImpl {

        @Override
        public int exec(DCOp[] program, int[] registers, int ip) {
            var op = program[ip];
            var dst = op.dst;
            var src = decodeOp(op.immediates, SRC_MASK, op.src, registers);
            var op1 = decodeOp(op.immediates, OP_MASK, op.op1, registers);
            registers[dst] = src + op1;
            return program[ip + 1].impl.exec(program, registers, ip + 1);
        }

    }

    private class GOTO implements DCOpImpl {

        @Override
        public int exec(DCOp[] program, int[] registers, int ip) {
            var op = program[ip];
            return decodeOp(op.immediates, DST_MASK, op.dst, registers);
        }
    }

    private class RETURN implements DCOpImpl {

        @Override
        public int exec(DCOp[] program, int[] registers, int ip) {
            return Integer.MAX_VALUE;
        }
    }

    record DCOp(int op, int immediates, int dst, int src, int op1, DCOpImpl impl){

        static final int IFLT = 0;
        static final int ADD = 1;
        static final int GOTO = 2;
        static final int RETURN = 3;

        static final int DST_MASK = 0b100;
        static final int SRC_MASK = 0b010;
        static final int OP_MASK = 0b001;
    }

    private static int decodeOp(int immediates, int mask, int payload, int[] registers){
        return (immediates & mask) == 0 ? registers[payload] : payload;
    }

    public Callable<Object> decodeChainedDispatch(){

        // Opcodes:
        // 0 - IFLT
        // 1 - ADD
        // 2 - GOTO
        // 3 - RETURN

        // Flags:
        // 0 - register
        // 1 - immediate

        var program = new DCOp[]{
                new DCOp(ADD, OP_MASK, 0, 0, 1, new ADD()),
                new DCOp(DCOp.IFLT, DCOp.SRC_MASK | OP_MASK, 0, 1000000, 0, new IFLT()),
                //new DCOp(GOTO, DST_MASK, 0, 0, 0, new GOTO()),
                new DCOp(RETURN, 0, 0, 0, 0, new RETURN())
        };

        return () -> {
            var registers = new int[1];

            int ip = 0;
            while(ip < program.length){
                var op = program[ip];
                ip = op.impl.exec(program, registers, ip);
            }
            return registers[0];
        };
    }

}
