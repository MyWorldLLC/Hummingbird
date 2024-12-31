package myworld.hummingbird.profiling;

public class GetLocal implements EvalNode {

    protected final int index;

    public GetLocal(int index){
        this.index = index;
    }

    public long eval(Invocation i) {
        return i.locals.locals[index];
    }
}
