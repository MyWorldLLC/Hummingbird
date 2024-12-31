package myworld.hummingbird.profiling;

import myworld.hummingbird.test.TestPrograms;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class Profiler {

    public static void main(String[] args) throws Exception {
        if(args.length > 1){
            System.out.println("Warning: profiler can accept at most 1 program name argument");
        }

        var program = args.length == 1 ? args[0] : "countOneMillion";

        new Profiler().run(program);
    }

    public void run(String program) throws Exception {

        var chosen = chooseProgram(program);

        System.out.println("Starting profiler for program " + program + ". Press Ctrl-C to exit.");
        while(true){
            Object value;
            long startTime = System.nanoTime();
            value = chosen.call();
            long endTime = System.nanoTime();

            System.out.println("Value: " + value + ", Time: " + (endTime - startTime) / 1e9 + " seconds");
            Thread.sleep(100);
        }

    }

    protected Callable<Object> chooseProgram(String program){

        var testPrograms = new TestPrograms();

        var programs = new HashMap<String, Callable<Object>>();
        testPrograms.javaTestPrograms()
                .forEach((name, callable) -> programs.put(name + "Java", callable));

        testPrograms.hvmTestPrograms()
                        .forEach((name, vm) -> programs.put(name, vm::run));

        countOneMillionNodes(programs);
        mathBenchNodes(programs);
        fib30Nodes(programs);

        return programs.get(program);
    }

    protected void countOneMillionNodes(HashMap<String, Callable<Object>> programs){

        var initX = new SetLocal(0, new Const(0));
        var condition = new ILe(new GetLocal(0), new Const(1000000));

        var loop = new While(condition, 2, 4);

        var setX = new SetLocal(0, new IAdd(new GetLocal(0),
                new Const(1)));

        var restartLoop = new Goto(1);

        var ret = new Return(new GetLocal(0));

        var f = new Function(0, 1, initX, loop, setX, restartLoop, ret);
        var interpreter = new Interpreter(new Functions(f));

        programs.put("countOneMillionNodes", () -> interpreter.call(0));
    }

    protected void mathBenchNodes(HashMap<String, Callable<Object>> programs){

        /*var getI = new GetLocal(0);
        var getX = new GetLocal(1);
        var initI = new SetLocal(0, new Const(0));
        var initX = new SetLocal(1, new Const(1.0));
        var testI = new ILe(new GetLocal(0), new Const(99999999));

        var calcI = new IAdd(new IAdd(new IMul(getI, new Const(2)), new IAdd(getI, getI)), new Const(1));
        var calcX = new DDiv(new DSub(new L2D(calcI), new Const(0.379)), getX);
        var setX = new SetLocal(1, calcX);


        var incI = new SetLocal(0, new IAdd(new GetLocal(0),
                new Const(1)));

        var loop = new While(testI, new Sequence(setX, incI));
        var ret = new Return(getX);

        var body = new Sequence(initI, initX, loop, ret);

        var f = new Function(0, 2, body);
        var interpreter = new Interpreter(new Functions(f));

        programs.put("mathBenchNodes", () -> interpreter.call(0));*/
    }

    protected void fib30Nodes(HashMap<String, Callable<Object>> programs){

        var getN = new GetLocal(0);

        var _if = new If(new ILe(getN, new Const(1)), new Return(getN));
        var call1 = new Call(0, 1,new ISub(getN, new Const(1)));
        var call2 = new Call(0, 2, new ISub(getN, new Const(2)));
        var recurse = new Return(new IAdd(new ThunkResult(1), new ThunkResult(2)));

        var f = new Function(1, 1, _if, call1, call2, recurse);
        var interpreter = new Interpreter(new Functions(f));

        programs.put("fib30Nodes", () -> interpreter.call(0, 30));
    }

    protected void callOneMillionNodes(HashMap<String, Callable<Object>> programs){

        /*var retOne = new Return(new Const(1));
        var f0 = new Function(0, 0, retOne);

        var initX = new SetLocal(0, new Const(0));
        var condition = new ILe(new GetLocal(0), new Const(1000000));

        var setX = new SetLocal(0, new IAdd(new GetLocal(0),
                new ThunkResult(1)));

        var loop = new While(condition, setX);

        var callF0 = new Call(0, 1);





        var ret = new Return(new GetLocal(0));

        var body = new Sequence(initX, setX, loop, ret);

        var f1 = new Function(0, 1, _if, call1, call2, recurse);
        var interpreter = new Interpreter(new Functions(f0, f1));

        programs.put("callOneMillionNodes", () -> interpreter.call(0));*/
    }

}
