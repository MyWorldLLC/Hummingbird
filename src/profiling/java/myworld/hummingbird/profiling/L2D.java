package myworld.hummingbird.profiling;

public class L2D implements EvalNode {

    protected final EvalNode exp;

    public L2D(EvalNode exp){
        this.exp = exp;
    }

    @Override
    public long eval(Invocation i) {
        return Double.doubleToLongBits((double) exp.eval(i));
    }
}
