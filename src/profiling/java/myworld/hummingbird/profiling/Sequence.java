package myworld.hummingbird.profiling;

public class Sequence implements EvalNode {

    protected final EvalNode[] children;

    public Sequence(EvalNode... children) {
        this.children = children;
    }

    @Override
    public long eval(Invocation i) {
        var value = 0L;
        for(var child : children){
            value = child.eval(i);
        }
        return value;
    }

}
