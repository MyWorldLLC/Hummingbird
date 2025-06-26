package myworld.hummingbird;

public class TrapMemoryExhaustionException extends TrapHandlingException {

    public TrapMemoryExhaustionException(int trapCode, int ip, Fiber fiber, Throwable t) {
        super("Memory fully exhausted while attempting to spawn trap handler", trapCode, ip, fiber, t);
    }
}
