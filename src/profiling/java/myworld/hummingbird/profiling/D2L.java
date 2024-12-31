package myworld.hummingbird.profiling;

import static java.lang.Double.longBitsToDouble;

public class D2L implements EvalNode {

    protected final EvalNode exp;

    public D2L(EvalNode exp){
        this.exp = exp;
    }

    @Override
    public long eval(Invocation i) {
        return (long) longBitsToDouble(exp.eval(i));
    }
}
