package myworld.hummingbird;

public class Frame {

    protected final Frame parent;
    protected final Registers registers;
    protected int ip;

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

}
