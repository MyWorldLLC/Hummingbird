package myworld.hummingbird.profiling;

import static java.lang.Double.doubleToLongBits;

public class Const implements EvalNode {

    protected final long value;

    public Const(long value){
        this.value = value;
    }

    public Const(double value){
        this.value = doubleToLongBits(value);
    }

    public long eval(Invocation i) {
        return value;
    }
}
