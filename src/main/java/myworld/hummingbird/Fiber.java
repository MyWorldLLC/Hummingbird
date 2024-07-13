package myworld.hummingbird;

public class Fiber {

    protected final Frame root;
    protected Frame current;

    public Fiber(Frame root){
        this.root = root;
        current = root;
    }

    public Frame root(){
        return root;
    }

    public Frame current(){
        return current;
    }

    public Frame parent(){
        return current.parent();
    }

    public Frame startCall(Registers registers){
        var frame = new Frame(current, registers);
        current = frame;
        return frame;
    }

    public void endCall(){
        current = current.parent();
    }

}
