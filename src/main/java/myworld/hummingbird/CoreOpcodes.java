package myworld.hummingbird;

public class CoreOpcodes {

    @Disassembles("CONST")
    public static final int CONST = 0x00;
    @Disassembles("ADD")
    public static final int ADD = 0x01;
    @Disassembles("IFLT")
    public static final int IFLT = 0x02;

    @Disassembles("RETURN")
    public static final int RETURN = 0x03;

    @Assembles("CONST")
    public static Opcode CONST(@Register Integer dst, @Immediate Integer src){
        return new Opcode(CONST, dst, src);
    }

    @Assembles("ADD")
    public static Opcode ADD(@Register Integer dst, @Register Integer src){
        return new Opcode(ADD, dst, src);
    }

    @Assembles("IFLT")
    public static Opcode IFLT(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(IFLT, dst, src, target);
    }

    @Assembles("RETURN")
    public static Opcode RETURN(@Register Integer src){
        return new Opcode(RETURN, src);
    }

}
