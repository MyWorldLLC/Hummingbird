package myworld.hummingbird;

public class HummingbirdException extends RuntimeException {

    private final int ip;
    private final Fiber fiber;

    public HummingbirdException(int ip, Fiber fiber, Throwable t){
        this(null, ip, fiber, t);
    }
    public HummingbirdException(String msg, int ip, Fiber fiber, Throwable t){
        super(msg, t);
        this.ip = ip;
        this.fiber = fiber;
    }

    public int getIp(){
        return ip;
    }

    public Fiber getFiber(){
        return fiber;
    }
}
