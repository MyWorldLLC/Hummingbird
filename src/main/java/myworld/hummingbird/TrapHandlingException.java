package myworld.hummingbird;

public abstract class TrapHandlingException extends HummingbirdException {

    protected final int trapCode;

    public TrapHandlingException(String msg, int trapCode, int ip, Fiber fiber, Throwable t) {
        super(msg, ip, fiber, t);
        this.trapCode = trapCode;
    }

    public int getTrapCode(){
        return trapCode;
    }
}
