package myworld.hummingbird;

public class SavedRegisters {

    protected int sizeInc;

    protected long[] lreg;
    protected int lIndex;

    protected IntStack stack;

    public SavedRegisters(int initialSize){
        sizeInc = initialSize;
        lreg = new long[initialSize];

        stack = new IntStack(initialSize);
    }

    public void clear(){
        lIndex = 0;
        stack.clear();
    }

    public void saveCallContext(int ip, int regOffset, int returnDest){
        stack.pushCtx(ip, regOffset, returnDest);
    }

    public void restoreCallContext(CallContext ctx){
        stack.popCtx(ctx);
    }

    public int callerRegisterOffset(){
        return stack.peekCallerRegisterOffset();
    }

    public void save(long[] reg, int index, int count){
        if(lreg.length <= lIndex + index + count){
            var tmp = lreg;
            lreg = new long[lreg.length + sizeInc];
            System.arraycopy(tmp, 0, lreg, 0, tmp.length);
        }
        System.arraycopy(reg, index, lreg, lIndex, count);
        lIndex += count;
    }

    public void restore(long[] reg, int index, int count){
        lIndex -= count;
        System.arraycopy(lreg, lIndex, reg, index, count);
    }

}
