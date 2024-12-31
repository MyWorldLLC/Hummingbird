package myworld.hummingbird.profiling;

public class IMul implements EvalNode {

    protected final EvalNode l;
    protected final EvalNode r;

    public IMul(EvalNode l, EvalNode r){
        this.l = l;
        this.r = r;
    }

    @Override
    public long eval(Invocation i) {
        return l.eval(i) * r.eval(i);
    }

}
