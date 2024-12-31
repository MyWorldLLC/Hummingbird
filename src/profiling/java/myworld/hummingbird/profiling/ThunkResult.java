package myworld.hummingbird.profiling;

public class ThunkResult implements EvalNode {

    protected final int thunk;

    public ThunkResult(int thunk){
        this.thunk = thunk;
    }

    @Override
    public long eval(Invocation i) {
        return i.thunkResults[thunk];
    }
}
