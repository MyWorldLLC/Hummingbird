package myworld.hummingbird.profiling;

public class Invocation {

    protected final Function function;
    protected final Locals locals;

    protected int activeThunk = 0;
    protected int call = -1;
    protected boolean _goto = true;
    protected long[] params = null;
    protected long[] thunkResults;
    protected boolean returned;
    protected long result;

    public Invocation(Function f, Locals locals){
        function = f;
        this.locals = locals;
        thunkResults = new long[f.thunks.length];
    }

    public int getActiveThunk() {
        return activeThunk;
    }

    public void setActiveThunk(int activeThunk) {
        this.activeThunk = activeThunk;
    }

    public void _goto(int thunk) {
        this.activeThunk = thunk;
        _goto = true;
    }

    public long getResult() {
        return result;
    }

    public void setResult(long result) {
        this.result = result;
    }
}
