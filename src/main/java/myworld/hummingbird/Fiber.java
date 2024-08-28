package myworld.hummingbird;

public final class Fiber {

    public enum State {
        RUNNABLE,
        BLOCKED
    }

    protected State state;
    public final long[] registers;
    private final IntStack callStack;

    public final HummingbirdVM vm;
    public final Executable exe;

    public int ip;
    public int returnDest;
    public int registerOffset;

    public int trapTableAddr = -1;
    public int trapHandlerCount = 0;

    public Fiber(HummingbirdVM vm, Executable exe, long[] registers){
        state = State.RUNNABLE;
        this.vm = vm;
        this.exe = exe;
        this.registers = registers;
        callStack = new IntStack(1000);
    }

    public void setState(State state){
        this.state = state;
    }

    public State getState(){
        return state;
    }

    public long[] getRegisters() {
        return registers;
    }

    public void saveCallContext(int ip, int regOffset, int returnDest){
        callStack.pushCtx(ip, regOffset, returnDest);
    }

    public void restoreCallContext(){
        callStack.popCtx(this);
    }
}
