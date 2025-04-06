package myworld.hummingbird.util;

import java.util.Arrays;

public class RingBuffer {

    private int[] storage;
    private final int stride;

    public RingBuffer(int initialSize, int stride){
        storage = new int[initialSize];
        this.stride = stride;
    }

    public int getField(int ptr, int field){
        return storage[stride * ptr + field];
    }

    public void setField(int ptr, int field, int value){
        if(stride * ptr >= storage.length / stride){
            expand();
        }
        storage[stride * ptr + field] = value;
    }

    public void clear(int ptr){
        Arrays.fill(storage, stride * ptr, stride * (ptr + 1), 0);
    }

    public int size(){
        return storage.length / stride;
    }

    protected void expand(){
        var newStorage = new int[storage.length + storage.length / 2];
        System.arraycopy(storage, 0, newStorage, 0, storage.length);
        storage = newStorage;
    }


}
