package myworld.hummingbird.assembler;

public class AssemblyException extends Exception {

    public AssemblyException(String msg){
        super(msg);
    }

    public AssemblyException(String msg, Throwable cause){
        super(msg, cause);
    }

}
