package myworld.hummingbird.profiling;

public class ISub implements EvalNode {

    protected final EvalNode l;
    protected final EvalNode r;

    public ISub(EvalNode l, EvalNode r){
        this.l = l;
        this.r = r;
    }

    @Override
    public long eval(Invocation i) {
        return l.eval(i) - r.eval(i);
    }
}