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

        return programs.get(program);
    }

}
