package myworld.hummingbird.profiling;

import java.util.Stack;

public class Interpreter {

    protected Functions functions;

    public Interpreter(Functions functions){
        this.functions = functions;
    }

    public long call(int function, long... params){
        var calls = new Stack<Invocation>();
        var f = functions.functions[function];
        var locals = new Locals(f.pCount + f.lCount);
        // TODO - param length check
        System.arraycopy(params, 0, locals.locals, 0, params.length);
        Invocation i = new Invocation(f, locals);
        calls.push(i);
        while(!calls.empty()){
            i = calls.peek();
            while(i.activeThunk < i.function.thunks.length){
                if(i.call == -1){
                    i.result = f.call(i); // Call with continuation state
                    if(i.returned){
                        i.result = i.thunkResults[i.activeThunk - 1];
                        calls.pop();
                        if(!calls.empty()){
                            var prior = calls.peek();
                            prior.thunkResults[prior.activeThunk - 1] = i.result;
                            i = prior;
                        }
                    }
                }else{
                    // Call next function
                    var next = invoke(i.call);
                    i.call = -1;
                    // TODO - param length check
                    System.arraycopy(i.params, 0, next.locals.locals, 0, i.params.length);
                    calls.push(next);
                    i = next;
                }
            }
        }
        return i.result;
    }

    private Invocation invoke(int function){
        var f = functions.functions[function];
        var locals = new Locals(f.pCount + f.lCount);
        return new Invocation(f, locals);
    }

}
