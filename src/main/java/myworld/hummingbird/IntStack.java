package myworld.hummingbird;

public final class IntStack {

    private int[] ints;
    private int index;
    private int sizeInc;

    public IntStack(int initialSize){
        ints = new int[initialSize];
        index = 0;
        sizeInc = initialSize;
    }

    public void push(int i){
        if(ints.length <= index + 1){
            var tmp = ints;
            ints = new int[ints.length + sizeInc];
            System.arraycopy(tmp, 0, ints, 0, tmp.length);
        }
        ints[index] = i;
        index++;
    }

    public int pop(){
        index--;
        return ints[index];
    }

    public void clear(){
        index = 0;
    }

    public int peek(int depth){
        return ints[index - depth];
    }

}
