package myworld.hummingbird.test.util;

import myworld.hummingbird.Executable;
import myworld.hummingbird.HummingbirdVM;
import myworld.hummingbird.MemoryLimits;
import myworld.hummingbird.util.Allocator;
import myworld.hummingbird.util.Pointer;

import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static myworld.hummingbird.HummingbirdVM.NULL;

public class AllocatorSmokeTest {

    public static final int NO_ITERATION_LIMIT = -1;
    private static final int NO_OVERLAP = -1;

    public static class AllocationStats{
        AtomicLong succeeded = new AtomicLong();
        AtomicLong failed = new AtomicLong();

        public void success(){
            succeeded.incrementAndGet();
        }

        public void fail(){
            failed.incrementAndGet();
        }

        public long getSucceeded() {
            return succeeded.get();
        }

        public long getFailed() {
            return failed.get();
        }
    }

    public static void runSmokeTest(int iterationLimit){
        runSmokeTest(iterationLimit, System.nanoTime());
    }

    public static void runSmokeTest(int iterationLimit, long randomSeed){

        var random = new Random();
        random.setSeed(randomSeed);

        var vm = new HummingbirdVM(Executable.builder().build(), new MemoryLimits(2048, 0));
        var allocator = new Allocator(vm, 8);

        var iteration = new AtomicInteger(0);

        var stats = new AllocationStats();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> printStats(stats, iteration)));

        var timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                printStats(stats, iteration);
            }
        }, 5 * 1000L, 5 * 1000L);

        System.out.println("Starting smoke test");

        while(iterationLimit == NO_ITERATION_LIMIT || iteration.intValue() < iterationLimit){

            var sizes = sizes(random, 0.75f);
            var pointers = new int[sizes.length];

            for(int i = 0; i < pointers.length; i++){
                pointers[i] = allocator.malloc(sizes[i]);
            }

            checkSolution(pointers, sizes, randomSeed, iteration.intValue(), iterationLimit, stats);

            for(var ptr : pointers){
                allocator.free(ptr);
            }

            iteration.incrementAndGet();
        }
    }

    private static void checkSolution(int[] pointers, int[] sizes, long randomSeed, int iteration, int iterationLimit, AllocationStats stats){
        boolean pass = true;
        for(int i = 0; i < pointers.length; i++){
            var overlap = findOverlap(i, pointers, sizes, stats);
            if(overlap != NO_OVERLAP){
                pass = false;
            }
        }
        if(!pass){
            System.out.println("Iteration %d/%d: %s - seed %d".formatted(iteration, iterationLimit, pass ? "PASS" : "FAIL", randomSeed));
        }

    }

    private static int findOverlap(int index, int[] pointers, int[] sizes, AllocationStats stats){
        var ptr = pointers[index];
        var size = sizes[index];

        // Note that allocations can fail due to fragmentation & load factor, so some pointers may be null
        if(ptr == NULL){
            stats.fail();
            return NO_OVERLAP;
        }
        stats.success();

        for(int i = 0; i < pointers.length; i++){
            if(i != index){
                var tPtr = pointers[i];
                var tSize = sizes[i];

                if(tPtr == NULL){
                    stats.fail();
                    continue;
                }

                stats.success();

                if(inRange(ptr, tPtr, tPtr + tSize) || inRange(ptr + size, tPtr, tPtr + tSize)){
                    System.out.println("Failed pointer: ptr %s, size %d, test range %s - %s".formatted(Pointer.toString(ptr), size, Pointer.toString(tPtr), Pointer.toString(tPtr + tSize)));
                    return i;
                }
            }
        }

        return NO_OVERLAP;
    }

    private static boolean inRange(int test, int a, int b){
        return a <= test && b >= test;
    }

    private static int[] sizes(Random random, float loadFactor){
        var totalSize = 0;
        var builder = IntStream.builder();

        while((float) totalSize / 2048 < loadFactor){
            var next = random.nextInt(1, 64);
            totalSize += next;
            builder.add(next);
        }

        return builder.build().toArray();
    }

    private static void printStats(AllocationStats stats, AtomicInteger iteration){
        var successes = stats.getSucceeded();
        var failures = stats.getFailed();
        var rate = successes / (double) (successes + failures) * 100.0;
        System.out.println("Completed %d iterations".formatted(iteration.intValue()));
        System.out.println("Allocations: succeeded %d, failed %d, ratio %f".formatted(successes, failures, rate));
    }

    public static void main(String[] args){
        runSmokeTest(NO_ITERATION_LIMIT);
    }
}
