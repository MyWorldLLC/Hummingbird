package myworld.hummingbird.profiling;

public class SetLocal implements EvalNode {

    protected final int index;
    protected final EvalNode exp;

    public SetLocal(int index, EvalNode exp){
        this.index = index;
        this.exp = exp;
    }

    @Override
    public long eval(Invocation i) {
        return i.locals.locals[index] = exp.eval(i);
    }
}
