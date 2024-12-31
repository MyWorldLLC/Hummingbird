package myworld.hummingbird.profiling;

public class If implements EvalNode {

    protected final EvalNode test;
    protected final EvalNode body;
    protected final EvalNode elseBody;

    public If(EvalNode test, EvalNode body){
        this(test, body, null);
    }

    public If(EvalNode test, EvalNode body, EvalNode elseBody){
        this.test = test;
        this.body = body;
        this.elseBody = elseBody;
    }

    @Override
    public long eval(Invocation i) {
        if(test.eval(i) != 0){
            return body.eval(i);
        }
        return elseBody != null ? elseBody.eval(i) : 0;
    }

}
