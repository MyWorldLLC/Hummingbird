package myworld.hummingbird;

public class InvalidTrapHandlerException extends TrapHandlingException {

    protected final int handler;

    public InvalidTrapHandlerException(int handler, int trapCode, int ip, Fiber fiber, Throwable t) {
        super("Invalid trap handler symbol index: " + handler, trapCode, ip, fiber, t);
        this.handler = handler;
    }

    public int getHandler(){
        return handler;
    }
}
