package myworld.hummingbird;

public class Fiber {

    public enum State {
        RUNNABLE,
        BLOCKED
    }

    protected State state;
    protected final long[] registers;
    protected final SavedRegisters savedRegisters;

    protected int regOffset;
    protected CallContext callCtx = new CallContext();

    public Fiber(long[] registers, SavedRegisters savedRegisters){
        state = State.RUNNABLE;
        this.registers = registers;
        this.savedRegisters = savedRegisters;
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

    public SavedRegisters getSavedRegisters() {
        return savedRegisters;
    }
}
