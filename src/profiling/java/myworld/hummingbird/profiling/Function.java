package myworld.hummingbird.profiling;

public class Function {

    protected final int pCount;
    protected final int lCount;
    protected final EvalNode[] thunks;
    // TODO - signature

    public Function(int pCount, int lCount, EvalNode... thunks){
        this.pCount = pCount;
        this.lCount = lCount;
        this.thunks = thunks;
    }

    public long call(Invocation i){
        var thunk = i.activeThunk;
        var result = thunks[i.activeThunk].eval(i);
        i.thunkResults[thunk] = result;
        if(!i._goto){
            i.activeThunk = thunk + 1;
        }else{
            i._goto = false;
        }

        return result;
    }

}
