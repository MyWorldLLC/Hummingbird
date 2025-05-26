package myworld.hummingbird.util;

public class IntSet {

    private final IntHashTable table;

    public IntSet(){
        this(64);
    }

    public IntSet(int initialSize){
        table = new IntHashTable(initialSize);
    }

    public void add(int v){
        table.insert(v, 1);
    }

    public boolean contains(int v){
        return table.contains(v);
    }

    public boolean remove(int v){
        return table.remove(v) != 0;
    }

    public int[] elements(){
        return table.keys();
    }

}
