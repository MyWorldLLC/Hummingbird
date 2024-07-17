package myworld.hummingbird;

public class Fiber {

    public enum State {
        RUNNABLE,
        BLOCKED
    }

    protected State state;
    protected final Registers registers;
    protected final SavedRegisters savedRegisters;

    public Fiber(Registers registers, SavedRegisters savedRegisters){
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

    public Registers getRegisters() {
        return registers;
    }

    public SavedRegisters getSavedRegisters() {
        return savedRegisters;
    }
}
