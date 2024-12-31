package myworld.hummingbird.profiling;

public class Return implements EvalNode {

    protected final EvalNode exp;

    public Return(EvalNode exp){
        this.exp = exp;
    }

    @Override
    public long eval(Invocation i) {
        i.returned = true;
        return exp.eval(i);
    }
}
