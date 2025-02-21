package myworld.hummingbird;

public final class Fiber {

    public static final int CALL_FRAME_SAVED_REGISTERS = 2;

    public enum State {
        RUNNABLE,
        BLOCKED
    }

    private State state;
    public final long[] registers;

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

    public int saveCallContext(int ip, int regOffset, int returnDest){
        //System.out.println("Saving call context: ip: " + ip + ", regOffset: " + regOffset + ", returnDest: " + returnDest);
        registers[regOffset + returnDest] = ip;
        registers[regOffset + returnDest + 1] = regOffset;
        registerOffset = regOffset + returnDest + 2;
        return registerOffset;
    }

    public int restoreCallContext(){
        ip = (int) registers[registerOffset - 2];
        registerOffset = (int) registers[registerOffset - 1];
        //System.out.println("Restoring call context: ip: " + ip + ", regOffset: " + registerOffset);
        return ip;
    }

    public int callerRegisterOffset(){
        return (int)registers[registerOffset - 1];
    }
}
