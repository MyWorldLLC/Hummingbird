package myworld.hummingbird.profiling;

public class Call implements EvalNode {

    protected final int function;
    protected final int thunk;
    protected final int pCount;
    protected final EvalNode[] params;

    public Call(int function, int thunk, EvalNode... params) {
        this.function = function;
        this.pCount = params.length;
        this.thunk = thunk;
        this.params = params;
    }

    @Override
    public long eval(Invocation i) {
        i.call = function;
        var c = new long[params.length];
        for(int p = 0; p < params.length; p++){
            c[p] = params[p].eval(i);
        }
        i.params = c;
        return 0xABCDEFABCDEFABCDL;
    }
}
