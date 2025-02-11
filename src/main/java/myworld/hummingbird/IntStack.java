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

    public void pushCtx(int ip, int registerOffset, int returnDest){
        var i = index;
        if(ints.length <= i + 3){
            var tmp = ints;
            ints = new int[ints.length + sizeInc];
            System.arraycopy(tmp, 0, ints, 0, tmp.length);
        }
        ints[i] = ip;
        ints[i + 1] = registerOffset;
        ints[i + 2] = returnDest;
        index += 3;
    }

    public void popCtx(Fiber fiber){
        var i = index;
        fiber.returnDest = ints[i - 1];
        fiber.registerOffset = ints[i - 2];
        fiber.ip = ints[i - 3];
        index -= 3;
    }

    public int peekCallerRegisterOffset(){
        return ints[index - 2];
    }

    public int pop(){
        index--;
        return ints[index];
    }

    public void setIndex(int rbp){
        index = rbp;
    }

    public void clear(){
        index = 0;
    }

    public int peek(int depth){
        return ints[index - depth];
    }

}
