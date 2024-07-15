package myworld.hummingbird;

public class Frame {

    protected final Frame parent;
    protected final Registers registers;
    protected int ip;
    protected int returnTarget;

    public Frame(Frame parent, Registers registers){
        this.parent = parent;
        this.registers = registers;
    }

    public Frame parent(){
        return parent;
    }

    public Registers registers(){
        return registers;
    }

    public int ip(){
        return ip;
    }

    public void setIp(int ip){
        this.ip = ip;
    }

    public int returnTarget(){
        return returnTarget;
    }

    public void setReturnTarget(int targetReg){
        returnTarget = targetReg;
    }

    public static Frame hostFrame(){
        var frame = new Frame(null, new Registers());
        frame.setIp(Integer.MAX_VALUE);
        frame.setReturnTarget(0);
        return frame;
    }

}
