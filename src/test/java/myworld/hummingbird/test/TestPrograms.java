package myworld.hummingbird.test;

import myworld.hummingbird.HummingbirdException;
import myworld.hummingbird.HummingbirdVM;
import myworld.hummingbird.assembler.Assembler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
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
        programs.put("mathBench", this::mathBench);
        programs.put("fibonacci30", this::fibonacci30);
        programs.put("decodeChainedDispatch", decodeChainedDispatch());
        programs.put("astCountOneMillion", astCountOneMillion());
        programs.put("astCallOneMillion", astCallOneMillion());
        programs.put("astFibonacci30", astFibonacci30());
        programs.put("astMathBench", astMathBench());
        programs.put("astCountAndFib", combined(astCountOneMillion(), astFibonacci30()));

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

    public float mathBench(){
        float x = 1.0f;
        for(int i = 0; i<99999999; i++){
           x = (i + i + 2 * i + 1 - 0.379f)/x;
        }
        return x;
    }

    private int fib(int n){
        if(n < 2){
            return n;
        }
        return fib(n - 1) + fib(n - 2);
    }

    public int fibonacci30(){
        return fib(30);
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

    private Callable<Object> combined(Callable<Object> a, Callable<Object> b){

        return () -> {
            var resultA = a.call();
            var resultB = b.call();
            return ((Integer) resultA) + ((Integer) resultB);
        };
    }

    public Callable<Object> astCountOneMillion(){

        var ctx = new Context();

        var initX = new SetVar(0);
        initX.value = new Const(0);

        var getX = new GetVar(0);

        var test = new Iflt();
        test.l = getX;
        test.r = new Const(1000000);

        var add = new Add();
        add.l = new GetVar(0);
        add.r = new Const(1);

        var body = new SetVar(0);
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

        var getN = new GetVar(0);

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

        var callOne = new CallNode(2);
        callOne.f = fib;
        callOne.args = new Node[]{subOne};

        var callTwo = new CallNode(2);
        callTwo.f = fib;
        callTwo.args = new Node[]{subTwo};

        var addFib = new Add();
        addFib.l = callOne;
        addFib.r = callTwo;

        var recursiveReturn = new ReturnNode();
        recursiveReturn.e = addFib;

        _if._else = recursiveReturn;

        fib.nodes = new Node[]{_if};

        var callFib = new CallNode(2);
        callFib.f = fib;
        callFib.args = new Node[]{new Const(30)};
        return () -> callFib.execute(ctx);
    }

    private Callable<Object> astMathBench(){
        /*
        # int i = 0;
        # double x = 1.0;
        # for(i = 0; i<99999999; i++){
        #   x = (i + i + 2 * i + 1 - 0.379)/x;
        # }
         */

        var ctx = new Context();

        var initI = new SetVar(0); // i = 0
        initI.value = new Const(0);

        var initX = new SetVar(1); // x = 1.0f
        initX.value = new Const(1.0f);

        var getI = new GetVar(0);
        var getX = new GetVar(1);

        var test = new Iflt();
        test.l = getI;
        test.r = new Const(99999999); // i < 99999999

        var addIPlusI = new Add(); // i + i
        addIPlusI.l = getI;
        addIPlusI.r = getI;

        var twoI = new Mul(); // 2 * i
        twoI.l = new Const(2);
        twoI.r = getI;

        var addTwoI = new Add(); // i + i + 2 * i
        addTwoI.l = addIPlusI;
        addTwoI.r = twoI;

        var plus1 = new Add(); // i + i + 2 * i + 1
        plus1.l = addTwoI;
        plus1.r = new Const(1);

        var toFloat = new I2F(); // (float)(i + i + 2 * i + 1)
        toFloat.i = plus1;

        var fSub = new FSub(); // (float)(i + i + 2 * i + 1) - 0.379
        fSub.l = toFloat;
        fSub.r = new Const(0.379f);

        var div = new FDiv(); // ((float)(i + i + 2 * i + 1) - 0.379) / x
        div.l = fSub;
        div.r = getX;

        var setX = new SetVar(1); // x = ((float)(i + i + 2 * i + 1) - 0.379) / x
        setX.value = div;

        var addIPlus1 = new Add(); // i++
        addIPlus1.l = getI;
        addIPlus1.r = new Const(1);
        var incrementI = new SetVar(0);
        incrementI.value = addIPlus1;

        var loop = new For();
        loop.pre = initI;
        loop.test = test;
        loop.body = setX;
        loop.post = incrementI;

        var program = new FunctionNode();
        program.nodes = new Node[]{initX, loop, getX};

        return () -> Float.intBitsToFloat(program.execute(ctx));
    }

    private Callable<Object> astCallOneMillion(){
        var ctx = new Context();

        var f = new FunctionNode();
        f.nodes = new Node[]{new Const(1)};

        var initX = new SetVar(0);
        initX.value = new Const(0);

        var getX = new GetVar(0);

        var test = new Iflt();
        test.l = getX;
        test.r = new Const(1000000);

        var callF = new CallNode(1);
        callF.args = new Node[0];
        callF.f = f;

        var xPlus1 = new Add();
        xPlus1.l = new GetVar(0);
        xPlus1.r = callF;

        var setX = new SetVar(0);
        setX.value = xPlus1;

        var loop = new For();
        loop.pre = initX;
        loop.test = test;
        loop.body = setX;

        var program = new FunctionNode();
        program.nodes = new Node[]{initX, loop, getX};

        return () -> program.execute(ctx);
    }

    private static class Suspension {
        int state;
        int value;
        NodePartial[] states;

        public Suspension(int state, NodePartial... states){
            this(state, 0, states);
        }

        public Suspension(int state, int value, NodePartial... states){
            this.state = state;
            this.value = value;
            this.states = states;
        }
    }

    private static class Context {
        int[] frame = new int[40];
        int framePtr = 0;
        int stackPtr = 0;
        int[] vars = new int[200];

        int interruptCounter = 0;
        volatile boolean interrupt;
        boolean _return;

        Object payload;
        Deque<Suspension> suspensions = new ArrayDeque<>();

        void markStackForCall(int locals){
            frame[framePtr] = stackPtr;
            framePtr++;
            stackPtr += locals;
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

        boolean checkInterrupt(){
            interruptCounter++;
            if(interruptCounter >= 100_000){
                return interrupt;
            }
            return false;
        }

        void repeat(NodePartial test, NodePartial body){
            int t = 0;
            try{
                t = test.execute(this, 0);
            }catch (Exception e){
                suspend(e, t, body, test);
            }

            while(t != 0 && !checkInterrupt()){
                try{
                    body.execute(this, 0);
                } catch (Exception e) {
                    suspend(e, t, body, test);
                }

                try{
                    t = test.execute(this, 0);
                } catch (Exception e) {
                    suspend(e, t, body, test);
                }
            }
        }

        void markForResume(Object state, NodePartial... resumables){
            // TODO push state + resumables.
        }

        void resume(Context ctx){
            var suspension = suspensions.pop();
            while(suspension != null){
                var value = suspension.value;
                for(int i = suspension.state; i < suspension.states.length; i++){
                    try {
                        value = suspension.states[i].execute(ctx, i);
                    }catch (Exception e){
                        ctx.suspend(e, value, Arrays.stream(suspension.states).skip(i - 1).toArray(NodePartial[]::new));
                    }
                }
                suspension = suspensions.pop();
            }

        }

        void suspendStateless(Throwable t, NodePartial... resumables) throws RuntimeException {
            suspend(t, 0, resumables);
        }

        void suspend(Throwable t, int state, NodePartial... resumables) throws RuntimeException {
            suspensions.add(new Suspension(state, resumables));
            throw new RuntimeException(t);
        }
    }

    private interface NodePartial {
        int execute(Context ctx, int state);
    }

    private interface StatelessNodePartial extends NodePartial {
        int execute(Context ctx);

        default int execute(Context ctx, int state){
            return execute(ctx);
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

        public Const(float f){
            value = Float.floatToIntBits(f);
        }

        @Override
        public int execute(Context ctx) {
            return value;
        }
    }

    private static class SetVar implements Node {
        Node value;
        int v;

        public SetVar(int v){
            this.v = v;
        }

        @Override
        public int execute(Context ctx) {
            var r = value.execute(ctx);
            return ctx.setLocal(v, r);
        }
    }

    private static class GetVar implements Node {
        int v;

        public GetVar(int v){
            this.v = v;
        }

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

    private static class Mul implements Node {
        Node l, r;

        @Override
        public int execute(Context ctx) {
            return l.execute(ctx) * r.execute(ctx);
        }
    }

    private static class FSub implements Node {
        Node l, r;

        @Override
        public int execute(Context ctx) {
            return Float.floatToIntBits(Float.intBitsToFloat(l.execute(ctx)) - Float.intBitsToFloat(r.execute(ctx)));
        }
    }

    private static class FDiv implements Node {
        Node l, r;

        @Override
        public int execute(Context ctx) {
            return Float.floatToIntBits(Float.intBitsToFloat(l.execute(ctx)) / Float.intBitsToFloat(r.execute(ctx)));
        }
    }

    private static class I2F implements Node {
        Node i;

        @Override
        public int execute(Context ctx) {
            return Float.floatToIntBits((float) i.execute(ctx));
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

    private static class Block implements Node {
        Node[] body;

        @Override
        public int execute(Context ctx) {
            int result = 0;
            for(int i = 0; i < body.length; i++){
                try{
                    result = body[i].execute(ctx);
                }catch (Exception e){
                    ctx.payload = 10;
                    throw e;
                }
            }
            return result;
        }
    }

    private static class While implements Node {
        Node test;
        Node body;

        @Override
        public int execute(Context ctx) {
            return doBody(ctx, doTest(ctx, 0));
        }

        private int doTest(Context ctx, int prior){
            try{
                return test.execute(ctx);
            }catch (Exception e){
                ctx.suspendStateless(e, this::doBody);
            }
            return 0;
        }

        private int doBody(Context ctx, int t){
            while(t != 0 && !ctx.checkInterrupt()){
                try{
                    body.execute(ctx);
                }catch (Exception e){
                    ctx.suspendStateless(e, this::doTest);
                }

                try{
                    t = test.execute(ctx);
                }catch (Exception e){
                    ctx.suspendStateless(e, this::doBody);
                }
            }
            return 0;
        }
    }

    private static class For implements Node {
        Node pre;
        Node test;
        Node body;
        Node post;

        @Override
        public int execute(Context ctx) {
            doPre(ctx, 0);
            return doBody(ctx, doTest(ctx, 0));
        }

        private int doPre(Context ctx, int prior){
            if(pre != null){
                try{
                    pre.execute(ctx);
                }catch (Exception e){
                    ctx.suspendStateless(e, this::doTest);
                }
            }
            return 0;
        }

        private int doTest(Context ctx, int prior){
            try{
                return test.execute(ctx);
            }catch (Exception e){
                ctx.suspendStateless(e, this::doBody);
            }
            return 0; // suspend() will rethrow so this will never be reached
        }

        private int doBody(Context ctx, int test){
            int t = test;
            while(t != 0 && !ctx.checkInterrupt()){
                try{
                    body.execute(ctx);
                }catch (Exception e){
                    ctx.suspendStateless(e, this::doPost);
                }

                doPost(ctx, 0);
                t = doTest(ctx, 0);
            }
            return 0;
        }

        private int doPost(Context ctx, int prior){
            if(post != null){
                try{
                    post.execute(ctx);
                }catch (Exception e){
                    ctx.suspendStateless(e, this::doTest);
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
        int locals = 0;
        Node f;
        Node[] args;

        public CallNode(int locals){
            this.locals = locals;
        }

        @Override
        public int execute(Context ctx) {
            for(int i = 0; i < args.length; i++){
                ctx.setLocal(i + locals, args[i].execute(ctx));
            }
            ctx.markStackForCall(locals);
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
