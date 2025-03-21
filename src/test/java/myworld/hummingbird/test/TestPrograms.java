package myworld.hummingbird.test;

import myworld.hummingbird.HummingbirdException;
import myworld.hummingbird.HummingbirdVM;
import myworld.hummingbird.assembler.Assembler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import static myworld.hummingbird.test.TestPrograms.DCOp.*;

public class TestPrograms {

    public final HummingbirdVM countOneMillion = load("myworld/hummingbird/test/programs/countOneMillion.hasm");
    public final HummingbirdVM callOneMillion = load("myworld/hummingbird/test/programs/callOneMillion.hasm");
    public final HummingbirdVM fibonacci30 = load("myworld/hummingbird/test/programs/fibonacci30.hasm");
    public final HummingbirdVM fibonacci30Call0 = load("myworld/hummingbird/test/programs/fibonacci30Call0.hasm");
    public final HummingbirdVM goldenRatio = load("myworld/hummingbird/test/programs/goldenRatio.hasm");
    public final HummingbirdVM memReadWrite = load("myworld/hummingbird/test/programs/memReadWrite.hasm");
    public final HummingbirdVM simpleFunction = load("myworld/hummingbird/test/programs/simpleFunction.hasm");
    public final HummingbirdVM simpleFiber = load("myworld/hummingbird/test/programs/simpleFiber.hasm");
    public final HummingbirdVM recursiveAdd = load("myworld/hummingbird/test/programs/recursiveAdd.hasm");
    public final HummingbirdVM recursiveCountOneMillion = load("myworld/hummingbird/test/programs/recursiveCountOneMillion.hasm");
    public final HummingbirdVM mathBench = load("myworld/hummingbird/test/programs/mathBench.hasm");

    public Map<String, Callable<Object>> javaTestPrograms(){
        var programs = new HashMap<String, Callable<Object>>();

        programs.put("countOneMillion", this::countOneMillion);
        programs.put("returnConstant", this::returnConstant);
        programs.put("decodeChainedDispatch", decodeChainedDispatch());
        programs.put("astCountOneMillion", astCountOneMillion());
        programs.put("astFibonacci30", astFibonacci30());

        return programs;
    }

    public Map<String, HummingbirdVM> hvmTestPrograms(){

        var programs = new HashMap<String, HummingbirdVM>();

        programs.put("countOneMillion", countOneMillion);
        programs.put("callOneMillion", callOneMillion);
        programs.put("fibonacci30", fibonacci30);
        programs.put("fibonacci30Call0", fibonacci30Call0);
        programs.put("memReadWrite", memReadWrite);
        programs.put("simpleFiber", simpleFiber);
        programs.put("simpleFunction", simpleFunction);
        programs.put("recursiveAdd", recursiveAdd);
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

        int exec(DCOp[] program, DCOp op, int[] registers, int[] memory, int ip);

    }

    private static int chainNext(DCOp[] program, int[] registers, int[] memory, int ip){
        try{
            var next = program[ip + 1];
            return next.impl().exec(program, next, registers, memory, ip + 1);
        }catch (HummingbirdException e){
            throw e;
        }catch (Throwable t){
            return Integer.MAX_VALUE;
        }
    }

    private class IFLT implements DCOpImpl {

        @Override
        public int exec(DCOp[] program, DCOp op, int[] registers, int[] memory, int ip) {
            var t1 = decodeOp(op.opFlags, R0, op.dst, registers, memory);
            var t2 = decodeOp(op.opFlags, R1, op.src, registers, memory);
            if(t1 < t2){
                ip = decodeOp(op.opFlags, R2, op.op1, registers, memory);
            }else{
                ip++;
            }
            return ip;
        }
    }

    private class ADD implements DCOpImpl {

        @Override
        public int exec(DCOp[] program, DCOp op, int[] registers, int[] memory, int ip) {
            var dst = op.dst;
            var src = decodeOp(op.opFlags, R1, op.src, registers, memory);
            var op1 = decodeOp(op.opFlags, R2, op.op1, registers, memory);
            registers[dst] = src + op1;
            return chainNext(program, registers, null, ip);
        }

    }

    private class GOTO implements DCOpImpl {

        @Override
        public int exec(DCOp[] program, DCOp op, int[] registers, int[] memory, int ip) {
            return decodeOp(op.opFlags, R0, op.dst, registers, memory);
        }
    }

    private class RETURN implements DCOpImpl {

        @Override
        public int exec(DCOp[] program, DCOp op, int[] registers, int[] memory, int ip) {
            return Integer.MAX_VALUE;
        }
    }

    record DCOp(DCOpImpl impl, int opFlags, int dst, int src, int op1){

        static final int R0 = 0 * 2;
        static final int R1 = 1 * 2;
        static final int R2 = 2 * 2;
        static final int R3 = 3 * 2;
        static final int R4 = 4 * 2;
        static final int R5 = 5 * 2;
        static final int R6 = 6 * 2;
        static final int R7 = 7 * 2;
        static final int R8 = 8 * 2;
        static final int R9 = 9 * 2;
        static final int R10 = 10 * 2;
        static final int R11 = 11 * 2;
        static final int R12 = 12 * 2;
        static final int R13 = 13 * 2;
        static final int RBP = 14 * 2;
        static final int RSP = 15 * 2;

        static final int OP_REG = 0b00;
        static final int OP_IMM = 0b01;
        static final int OP_MEM = 0b10;
    }

    static int encodeOp(int opType, int register){
        return opType << register;
    }

    private static int decodeOp(int operandTypes, int register, int payload, int[] registers, int[] memory){
        return switch ((operandTypes >> register) & 0b11){
            case OP_REG -> registers[payload];
            case OP_IMM -> payload;
            case OP_MEM -> memory[payload];
            default -> throw new IllegalArgumentException();
        };
    }

    public Callable<Object> decodeChainedDispatch(){

        var program = new DCOp[]{
                new DCOp(new ADD(), encodeOp(OP_IMM, R2), 0, 0, 1),
                new DCOp(new IFLT(), encodeOp(OP_IMM, R1) | encodeOp(OP_IMM, R2), 0, 1000000, 0),
                new DCOp(new RETURN(), 0, 0, 0, 0)
        };

        return () -> {
            var registers = new int[16];

            int ip = 0;
            while(ip < program.length){
                var op = program[ip];
                ip = op.impl.exec(program, op, registers, null, ip);
            }
            return registers[0];
        };
    }

    public Callable<Object> astCountOneMillion(){

        var ctx = new Context();

        var initX = new SetVar();
        initX.value = new Const(0);

        var getX = new GetVar();

        var test = new Iflt();
        test.l = getX;
        test.r = new Const(1000000);

        var add = new Add();
        add.l = new GetVar();
        add.r = new Const(1);

        var body = new SetVar();
        body.value = add;

        var loop = new While();
        loop.test = test;
        loop.body = body;

        var program = new FunctionNode();
        program.nodes = new Node[]{initX, loop, getX};

        return () -> program.execute(ctx);
    }

    public Callable<Object> astFibonacci30(){

        var ctx = new Context();
        var fib = new FunctionNode();

        var getN = new GetVar();

        var test = new Iflt();
        test.l = getN;
        test.r = new Const(2);

        var _if = new If();
        _if.test = test;

        var bail = new ReturnNode();
        bail.e = getN;

        _if._if = bail;

        var subOne = new Sub();
        subOne.l = getN;
        subOne.r = new Const(1);

        var subTwo = new Sub();
        subTwo.l = getN;
        subTwo.r = new Const(2);

        var callOne = new CallNode();
        callOne.f = fib;
        callOne.args = new Node[]{subOne};

        var callTwo = new CallNode();
        callTwo.f = fib;
        callTwo.args = new Node[]{subTwo};

        var addFib = new Add();
        addFib.l = callOne;
        addFib.r = callTwo;

        var recursiveReturn = new ReturnNode();
        recursiveReturn.e = addFib;

        _if._else = recursiveReturn;

        fib.nodes = new Node[]{_if};

        var callFib = new CallNode();
        callFib.f = fib;
        callFib.args = new Node[]{new Const(30)};
        return () -> callFib.execute(ctx);
    }

    private static class Context {
        int[] frame = new int[40];
        int framePtr = 0;
        int stackPtr = 0;
        int[] vars = new int[200];

        boolean interrupt;
        boolean _return;

        Object payload;

        void markStackForCall(){
            frame[framePtr] = stackPtr;
            framePtr++;
            stackPtr += 1; // TODO - make this variable, but for now assume a fixed frame of 1 local
        }

        void restoreStack(){
            framePtr--;
            stackPtr = frame[framePtr];
            _return = false;
        }

        int setLocal(int local, int value){
            vars[stackPtr + local] = value;
            return value;
        }

        int getLocal(int local){
            return vars[stackPtr + local];
        }
    }

    private interface Node {
        int execute(Context ctx);
    }

    private static class Const implements Node {
        int value;

        public Const(int v){
            value = v;
        }

        @Override
        public int execute(Context ctx) {
            return value;
        }
    }

    private static class SetVar implements Node {
        Node value;
        int v = 0;

        @Override
        public int execute(Context ctx) {
            var r = value.execute(ctx);
            return ctx.setLocal(v, value.execute(ctx));
        }
    }

    private static class GetVar implements Node {
        int v = 0;

        @Override
        public int execute(Context ctx) {
            return ctx.getLocal(v);
        }
    }

    private static class Add implements Node {
        Node l, r;

        @Override
        public int execute(Context ctx) {
            return l.execute(ctx) + r.execute(ctx);
        }
    }

    private static class Sub implements Node {
        Node l, r;

        @Override
        public int execute(Context ctx) {
            return l.execute(ctx) - r.execute(ctx);
        }
    }

    private static class Iflt implements Node {
        Node l, r;

        @Override
        public int execute(Context ctx) {
            int a;
            try {
                a = l.execute(ctx);
            } catch (Exception e) {
                ctx.payload = 10;
                throw e;
            }

            int b;
            try {
                b = r.execute(ctx);
            } catch (Exception e) {
                ctx.payload = 10;
                throw e;
            }
            return a < b ? 1 : 0;
        }
    }

    private static class If implements Node {
        Node test;
        Node _if;
        Node _else;

        @Override
        public int execute(Context ctx) {
            int t;
            try{
                t = test.execute(ctx);
            }catch (Exception e){
                ctx.payload = 10;
                throw e;
            }
            if(t != 0){
                try{
                    return _if.execute(ctx);
                }catch (Exception e){
                    ctx.payload = 10;
                    throw e;
                }
            }else if(_else != null){
                return _else.execute(ctx);
            }
            return 0;
        }
    }

    private static class While implements Node {
        Node test;
        Node body;

        @Override
        public int execute(Context ctx) {
            int t;
            try{
                t = test.execute(ctx);
            }catch (Exception e){
                ctx.payload = 10;
                throw e;
            }
            while(t != 0 && !ctx.interrupt){
                try{
                    body.execute(ctx);
                }catch (Exception e){
                    ctx.payload = 10;
                    throw e;
                }
                try{
                    t = test.execute(ctx);
                }catch (Exception e){
                    ctx.payload = 10;
                    throw e;
                }
            }
            return 0;
        }
    }

    private static class FunctionNode implements Node {
        private Node[] nodes;

        @Override
        public int execute(Context ctx) {
            for(int i = 0; i < nodes.length - 1; i++){
                var v = nodes[i].execute(ctx);
                if(ctx._return){
                    return v;
                }
            }
            return nodes[nodes.length - 1].execute(ctx);
        }
    }

    private static class CallNode implements Node {
        Node f;
        Node[] args;

        @Override
        public int execute(Context ctx) {
            for(int i = 0; i < args.length; i++){
                // TODO - this + 1 is just to align with the next call frame
                ctx.setLocal(i + 1, args[i].execute(ctx));
            }
            ctx.markStackForCall();
            int result = f.execute(ctx);
            ctx.restoreStack();
            return result;
        }
    }

    private static class ReturnNode implements Node {
        Node e;

        @Override
        public int execute(Context ctx) {
            var v = e.execute(ctx);
            ctx._return = true;
            return v;
        }
    }

}
