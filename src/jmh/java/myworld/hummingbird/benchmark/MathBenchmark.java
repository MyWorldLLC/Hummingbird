package myworld.hummingbird.benchmark;

import myworld.hummingbird.test.TestPrograms;
import org.openjdk.jmh.annotations.*;

public class MathBenchmark {

    @State(Scope.Thread)
    public static class Programs {

        public TestPrograms testPrograms;

        @Setup(Level.Trial)
        public void initialize() throws Throwable {
            testPrograms = new TestPrograms();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    public Object countToAMillionJava() {
        int x = 0;
        while(x < 1000000) {
            x = x + 1;
        }
        return x;
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    public static Object countToAMillionMicroVM(Programs programs){
        return programs.testPrograms.countOneMillion.run();
    }


}
