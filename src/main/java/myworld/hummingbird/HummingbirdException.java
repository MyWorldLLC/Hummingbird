package myworld.hummingbird;

public class HummingbirdException extends RuntimeException {

    private final int ip;
    private final long[] registers;

    public HummingbirdException(int ip, long[] registers, Throwable t){
        this(null, ip, registers, t);
    }
    public HummingbirdException(String msg, int ip, long[] registers, Throwable t){
        super(msg, t);
        this.ip = ip;
        this.registers = registers;
    }

    public int getIp(){
        return ip;
    }

    public long[] getRegisters(){
        return registers;
    }
}
