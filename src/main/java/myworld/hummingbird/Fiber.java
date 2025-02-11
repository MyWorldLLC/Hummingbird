package myworld.hummingbird;

import static myworld.hummingbird.Registers.RBP;
import static myworld.hummingbird.Registers.RSP;

public final class Fiber {

    public enum State {
        RUNNABLE,
        BLOCKED
    }

    protected State state;
    public final int[] registers;
    private final IntStack callStack;

    public final HummingbirdVM vm;
    public final Executable exe;

    public int ip;
    public int returnDest;
    public int registerOffset;

    public int trapTableAddr = -1;
    public int trapHandlerCount = 0;

    public Fiber(HummingbirdVM vm, Executable exe, int[] registers){
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

    public int[] getRegisters() {
        return registers;
    }

    public void saveCallContext(int ip, int regOffset, int returnDest){
        callStack.pushCtx(ip, regOffset, returnDest);
    }

    public void restoreCallContext(){
        callStack.popCtx(this);
    }

    public void prepareCall(int ip, int[] registers, int start, int count){
        callStack.push(ip);
        for(int i = start; i < count; i++){
            callStack.push(registers[i]);
        }
        callStack.push(start);
        callStack.push(count);
        callStack.push(registers[RBP]);
        callStack.push(registers[RSP]);
    }

    public int handleReturn(int[] registers, int start, int count){

        var rsp = callStack.pop();
        var rbp = callStack.pop();
        var dCount = callStack.pop();
        var dStart = callStack.pop();

        for(int i = dCount - 1 - dStart; i >= dStart; i--){
            if(i <= start || i >= start + count){
                registers[i] = callStack.pop();
            }
        }

        callStack.setIndex(rbp);
        var ip = callStack.peek(1);

        registers[RSP] = rsp;
        registers[RBP] = rbp;

        return ip;
    }

    public int callerRegisterOffset(){
        return callStack.peekCallerRegisterOffset();
    }
}
