package myworld.hummingbird;

public class CoreOpcodes {

    @Disassembles("CONST")
    public static final int CONST = 0x00;
    @Disassembles("ADD")
    public static final int ADD = 0x01;
    @Disassembles("COND")
    public static final int COND = 0x02;

    @Disassembles("RETURN")
    public static final int RETURN = 0x03;

    public static final int COND_LT = 0;
    public static final int COND_LE = 1;
    public static final int COND_EQ = 2;
    public static final int COND_GE = 3;
    public static final int COND_GT = 4;

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
        return new Opcode(COND, Opcode.encodeRegisterOperand(COND_LT, dst), src, target);
    }

    @Assembles("IFLE")
    public static Opcode IFLE(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(COND, Opcode.encodeRegisterOperand(COND_LE, dst), src, target);
    }

    @Assembles("IFEQ")
    public static Opcode IFEQ(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(COND, Opcode.encodeRegisterOperand(COND_EQ, dst), src, target);
    }

    @Assembles("IFGE")
    public static Opcode IFGE(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(COND, Opcode.encodeRegisterOperand(COND_GE, dst), src, target);
    }

    @Assembles("IFGT")
    public static Opcode IFGT(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(COND, Opcode.encodeRegisterOperand(COND_GT, dst), src, target);
    }

    @Assembles("RETURN")
    public static Opcode RETURN(@Register Integer src){
        return new Opcode(RETURN, src);
    }

}
