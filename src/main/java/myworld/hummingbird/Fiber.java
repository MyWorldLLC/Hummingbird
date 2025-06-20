package myworld.hummingbird;

public final class Fiber {

    public static final int CALL_FRAME_SAVED_REGISTERS = 2;

    public enum State {
        RUNNABLE,
        BLOCKED
    }

    private State state;
    private final int stackBase;
    private final int stackMax;

    public final HummingbirdVM vm;
    public final Executable exe;

    public int ip;
    public int returnDest;
    public int registerOffset;

    public int trapTableAddr = -1;
    public int trapHandlerCount = 0;

    public Fiber(HummingbirdVM vm, Executable exe, int stackBase, int stackSize){
        state = State.RUNNABLE;
        this.vm = vm;
        this.exe = exe;
        this.stackBase = stackBase;
        stackMax = stackBase + stackSize;
        registerOffset = stackBase;
    }

    public int getStackSize(){
        return stackMax - stackBase;
    }

    public void setState(State state){
        this.state = state;
    }

    public State getState(){
        return state;
    }

    public int saveCallContext(int ip, int returnDest){
        register(returnDest, ip);
        register(returnDest + 1, registerOffset);
        registerOffset += returnDest + 2;
        return registerOffset;
    }

    public int restoreCallContext(){
        ip = register(-2);
        var oldOffset = registerOffset;
        registerOffset = register(-1);
        return oldOffset - registerOffset - 2; // Return destination register
    }

    public void register(int r, int value){
        vm.writeInt(r + registerOffset, value);
    }

    public int register(int r){
        return vm.readInt(r + registerOffset);
    }

    public void longRegister(int r, long value){
        vm.writeLong(r + registerOffset, value);
    }

    public long longRegister(int r){
        return vm.readLong(r + registerOffset);
    }

    public int regPointer(int r){
        return r + registerOffset;
    }

    public int callerRegisterOffset(){
        return register(registerOffset - 1);
    }
}
