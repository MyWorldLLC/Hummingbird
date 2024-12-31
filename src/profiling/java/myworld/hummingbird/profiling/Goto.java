package myworld.hummingbird.profiling;

public class Goto implements EvalNode {

    protected final int targetThunk;

    public Goto(int targetThunk){
        this.targetThunk = targetThunk;
    }

    @Override
    public long eval(Invocation i) {
        i._goto(targetThunk);
        return 0;
    }
}
