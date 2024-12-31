package myworld.hummingbird.profiling;

public class While implements EvalNode {

    protected final EvalNode test;
    protected final int bodyThunk;
    protected final int endThunk;

    public While(EvalNode test, int bodyThunk, int endThunk){
        this.test = test;
        this.bodyThunk = bodyThunk;
        this.endThunk = endThunk;
    }

    @Override
    public long eval(Invocation i) {
        if(test.eval(i) != 0){
            i._goto(bodyThunk);
        }else{
            i._goto(endThunk);
        }
        return 0;
    }

}
