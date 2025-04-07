package myworld.hummingbird.test.util;

import myworld.hummingbird.util.IntHashTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class IntHashTableTest {

    protected IntHashTable table;

    @BeforeEach
    public void setup(){
        table = new IntHashTable(100);
    }

    @Test
    public void insertAndGetKeys(){
        table.insert(1, 2);
        table.insert(3, 4);
        table.insert(5, 6);

        assertEquals(2, table.get(1));
        assertEquals(4, table.get(3));
        assertEquals(6, table.get(5));
    }

    @Test
    public void checkCount(){
        table.insert(1, 2);
        table.insert(3, 4);
        table.remove(3);
        table.remove(5);

        assertEquals(1, table.size());
    }

    @Test
    public void checkRemove(){
        table.insert(1, 2);
        table.insert(3, 4);

        table.remove(1);

        assertEquals(1, table.size());
        assertEquals(4, table.get(3));
        assertEquals(0, table.get(1));
    }

    @Test
    public void checkContains(){
        table.insert(1, 2);

        assertTrue(table.contains(1));
        assertFalse(table.contains(3));
    }

    @Test
    public void checkClear(){
        table.insert(1, 2);
        table.insert(3, 4);

        table.clear();

        assertEquals(0, table.size());
        assertEquals(0, table.get(1));
        assertFalse(table.contains(3));
    }

    @Test
    public void checkGrow(){

        for(int i = 1; i < 201; i++){
            table.insert(i, i);
        }

        assertEquals(200, table.size());

        for(int i = 1; i < 201; i++){
            assertEquals(i, table.get(i));
        }
    }

    @Test
    public void checkCopyTo(){
        var other = new IntHashTable(10);

        table.insert(1, 2);
        table.insert(3, 4);

        assertTrue(table.capacity() > 0);

        table.copyTo(other);

        assertEquals(2, other.size());
        assertEquals(2, other.get(1));
        assertEquals(4, other.get(3));
    }

    @Test
    public void checkGetKeys(){
        table.insert(1, 2);
        table.insert(3, 4);

        var keys = table.keys();
        Arrays.sort(keys);
        assertArrayEquals(new int[]{1, 3}, keys);
    }

    @Test
    public void checkGetValues(){
        table.insert(1, 2);
        table.insert(3, 4);

        var values = table.values();
        Arrays.sort(values);
        assertArrayEquals(new int[]{2, 4}, values);
    }

    @Test
    public void checkCompact(){
        for(int i = 1; i < 201; i++){
            table.insert(i, i);
        }

        assertEquals(200, table.size());

        for(int i = 1; i < 101; i++){
            table.remove(i);
        }

        assertEquals(100, table.size());
        var keys = table.keys();
        var values = table.values();
        table.compact();

        var compactedKeys = table.keys();
        var compactedValues = table.values();

        Arrays.sort(keys);
        Arrays.sort(compactedKeys);
        Arrays.sort(values);
        Arrays.sort(compactedValues);

        assertArrayEquals(keys, compactedKeys);
        assertArrayEquals(values, compactedValues);
    }

}
