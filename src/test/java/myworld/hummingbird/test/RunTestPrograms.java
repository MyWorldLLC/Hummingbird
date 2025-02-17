package myworld.hummingbird.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class RunTestPrograms {

    private static TestPrograms testPrograms;

    @BeforeAll
    static void setupTestPrograms(){
        testPrograms = new TestPrograms();
    }

    @Test
    void countOneMillion(){
        var result = testPrograms.countOneMillion.run();
        assertInstanceOf(Long.class, result);
        assertEquals(1000000L, result);
    }

    @Test
    void callOneMillion(){
        var result = testPrograms.callOneMillion.run();
        assertInstanceOf(Long.class, result);
        assertEquals(1000000L, result);
    }

    @Test
    void fibonacci30(){
        var result = testPrograms.fibonacci30.run();
        assertInstanceOf(Long.class, result);
        assertEquals(832040L, result);
    }

    @Test
    void goldenRatio(){
        var result = testPrograms.goldenRatio.run();
        assertInstanceOf(Double.class, result);
        assertEquals(1.6180339887787318, result);
    }

    @Test
    void mathBench(){
        var result = testPrograms.mathBench.run();
        assertInstanceOf(Long.class, result);
        assertEquals(4664475299226990914L, result);
    }

    @Test
    void simpleFiber(){
        var result = testPrograms.simpleFiber.run();
        assertInstanceOf(Long.class, result);
        assertEquals(5L, result);
    }

    @Test
    void simpleFunction(){
        var result = testPrograms.simpleFunction.run();
        assertInstanceOf(Long.class, result);
        assertEquals(7L, result);
    }
}
