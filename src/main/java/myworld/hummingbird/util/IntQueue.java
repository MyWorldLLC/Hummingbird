package myworld.hummingbird.util;

public class IntQueue {

    private int[] queue;
    private int read;
    private int write;

    public IntQueue(){
        this(64);
    }

    public IntQueue(int startSize){
        queue = new int[startSize];
        read = write = 0;
    }

    public void push(int v){
        if(write == queue.length || write + 1 == read){
            if(read > 0){
                write = 0;
            }else{
                // Queue is full
                expand();
                push(v);
                return;
            }
        }
        queue[write] = v;
        write++;
    }

    public int pop(){
        if(isEmpty()){
            throw new IllegalStateException("Queue is empty");
        }

        if(read == queue.length){
            read = 0;
        }

        var v = queue[read];
        read++;
        return v;
    }

    public boolean isEmpty(){
        return read == write;
    }

    private void expand(){
        var next = new int[queue.length * 2];
        System.arraycopy(queue, 0, next, 0, queue.length);
        queue = next;
    }
}
