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
        assertInstanceOf(Integer.class, result);
        assertEquals(1000000, result);
    }

    @Test
    void callOneMillion(){
        var result = testPrograms.callOneMillion.run();
        assertInstanceOf(Integer.class, result);
        assertEquals(1000000, result);
    }

    @Test
    void fibonacci30(){
        var result = testPrograms.fibonacci30.run();
        assertInstanceOf(Integer.class, result);
        assertEquals(832040, result);
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
        assertInstanceOf(Double.class, result);
        assertEquals(7051.571197642306, result);
    }

    @Test
    void simpleFiber(){
        var result = testPrograms.simpleFiber.run();
        assertInstanceOf(Integer.class, result);
        assertEquals(5, result);
    }

    @Test
    void simpleFunction(){
        var result = testPrograms.simpleFunction.run();
        assertInstanceOf(Integer.class, result);
        assertEquals(7, result);
    }
}
