package myworld.hummingbird;

public class Frame {

    protected final Frame parent;
    protected final Registers registers;
    protected final Symbol symbol;
    protected final Params paramOffsets;
    protected int ip;
    protected int returnTarget;

    public Frame(Frame parent, Registers registers, Symbol symbol, Params paramOffsets){
        this.parent = parent;
        this.registers = registers;
        this.symbol = symbol;
        this.paramOffsets = paramOffsets;
    }

    public Frame parent(){
        return parent;
    }

    public Registers registers(){
        return registers;
    }

    public Symbol symbol(){
        return symbol;
    }

    public Params paramOffsets(){
        return paramOffsets;
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

    public static Frame hostFrame(Symbol symbol){
        var frame = new Frame(null, new Registers(), symbol, Params.zeroes());
        frame.setIp(Integer.MAX_VALUE);
        frame.setReturnTarget(0);
        return frame;
    }

    public String toString(){
        var builder = new StringBuilder();

        var frame = this;
        while(frame != null){
            builder.append(symbol.name() + "@" + (frame.ip() - 1) + "\n");
            frame = frame.parent();
        }

        return builder.toString();
    }

}
