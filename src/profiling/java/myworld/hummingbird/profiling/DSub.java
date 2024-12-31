package myworld.hummingbird.profiling;

import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.longBitsToDouble;

public class DSub implements EvalNode {

    protected final EvalNode l;
    protected final EvalNode r;

    public DSub(EvalNode l, EvalNode r){
        this.l = l;
        this.r = r;
    }

    @Override
    public long eval(Invocation i) {
        return doubleToLongBits(longBitsToDouble(l.eval(i)) - longBitsToDouble(r.eval(i)));
    }

}
