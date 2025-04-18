package myworld.hummingbird.util;

import java.util.Arrays;

import static myworld.hummingbird.util.FibHash.*;

/**
 * Only supports nonzero keys & values
 */
public class IntHashTable {

    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private final float loadFactor;
    private int count;
    private int slots;
    private int nominalSize;
    private int[] entries;

    public IntHashTable(){
        this(100);
    }

    public IntHashTable(int initialSize){
        this(initialSize, DEFAULT_LOAD_FACTOR);
    }

    public IntHashTable(int initialSize, float loadFactor){
        if(loadFactor <= 0.0 || loadFactor >= 1.0f){
            throw new IllegalArgumentException("Load factor must be in the range (0.0, 1.0)");
        }
        this.loadFactor = loadFactor;
        nominalSize = initialSize;
        slots = nextPowerOf2((int) (initialSize / loadFactor));
        entries = new int[slots * 2];
    }

    public void insert(int k, int v){
        if(k == 0 || v == 0){
            throw new IllegalArgumentException("Illegal k/v pair: (%d, %d)".formatted(k, v));
        }
        if(count + 1 >= nominalSize){
            growTable();
        }
        var nominalIndex = indexForKey(k);
        for(int i = nominalIndex; i < entries.length; i += 2){
            var eKey = entries[i];
            if(eKey == 0 || eKey == k){
                entries[i] = k;
                entries[i + 1] = v;
                if(eKey == 0){
                    count++;
                }
                return;
            }
        }
        // We didn't find a slot, so grow the table and try again. There are guaranteed to be free slots now.
        // This is incredibly unlikely to occur since we already check the load factor and grow the table
        // as needed before insert, but this fallback provides robustness against strange load factors.
        growTable();
        insert(k, v);
    }

    public int get(int k){
        if(k == 0){
            throw new IllegalArgumentException("Illegal key: %d".formatted(k));
        }
        var nominalIndex = indexForKey(k);
        for(int i = nominalIndex; i < entries.length; i += 2){
            var eKey = entries[i];
            if(eKey == k){
                return entries[i + 1];
            }
        }
        return 0;
    }

    public boolean contains(int k){
        if(k == 0){
            throw new IllegalArgumentException("Illegal key: %d".formatted(k));
        }
        var nominalIndex = indexForKey(k);
        for(int i = nominalIndex; i < entries.length; i += 2){
            var eKey = entries[i];
            if(eKey == k){
                return true;
            }
        }
        return false;
    }

    public int remove(int k){
        if(k == 0){
            throw new IllegalArgumentException("Illegal key: %d".formatted(k));
        }
        var nominalIndex = indexForKey(k);
        for(int i = nominalIndex; i < entries.length; i += 2){
            var eKey = entries[i];
            if(eKey == k){
                entries[i] = 0;
                var v = entries[i + 1];
                entries[i + 1] = 0;
                count--;
                return v;
            }
        }
        return 0;
    }

    public void clear(){
        Arrays.fill(entries, 0);
        count = 0;
    }

    public int size(){
        return count;
    }

    public int capacity(){
        return slots;
    }

    protected int indexForKey(int k){
        return hashToRange(hash(k), slots) << 1; // Multiply by 2 to convert a slot number to its equivalent strided index
    }

    public void copyTo(IntHashTable other){
        for(int i = 0; i < entries.length; i += 2){
            var k = entries[i];
            if(k != 0){
                other.insert(k, entries[i + 1]);
            }
        }
    }

    public void compact(){
        var other = new IntHashTable(count, loadFactor);
        copyTo(other);
        nominalSize = count;
        slots = other.slots;
        entries = other.entries;
    }

    public int[] keys(){
        var keys = new int[count];
        for(int i = 0, j = 0; i < entries.length; i += 2){
            var k = entries[i];
            if(k != 0){
                keys[j] = k;
                j++;
            }
        }
        return keys;
    }

    public int[] values(){
        var values = new int[count];
        for(int i = 0, j = 0; i < entries.length; i += 2){
            var k = entries[i];
            if(k != 0){
                values[j] = entries[i + 1];
                j++;
            }
        }
        return values;
    }

    private void growTable(){
        var oldTable = entries;
        slots = nextPowerOf2(slots + 1);
        nominalSize = (int) (slots * loadFactor);
        entries = new int[slots * 2];

        if(oldTable != null){
            count = 0;
            // This works because we've already snagged a copy of the entries and set the count to 0
            for(int i = 0; i < oldTable.length; i += 2){
                var k = oldTable[i];
                if(k != 0){
                    insert(k, oldTable[i + 1]);
                }
            }
        }
    }



}
