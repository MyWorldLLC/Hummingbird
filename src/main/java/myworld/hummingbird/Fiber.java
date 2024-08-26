package myworld.hummingbird;

public class Fiber {

    public enum State {
        RUNNABLE,
        BLOCKED
    }

    protected State state;
    public final long[] registers;
    public final SavedRegisters savedRegisters;

    public int regOffset;
    public CallContext callCtx = new CallContext();
    public final Executable exe;

    public Fiber(Executable exe, long[] registers, SavedRegisters savedRegisters){
        state = State.RUNNABLE;
        this.exe = exe;
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
