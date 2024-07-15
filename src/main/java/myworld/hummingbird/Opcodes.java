package myworld.hummingbird;

public class Opcodes {

    public static final int INT_T = 0;
    public static final int FLOAT_T = 1;
    public static final int LONG_T = 2;
    public static final int DOUBLE_T = 3;
    public static final int STRING_T = 4;
    public static final int OBJECT_T = 5;
    public static final int VOID_T = 6;


    public static final int COND_LT = 0;
    public static final int COND_LE = 1;
    public static final int COND_EQ = 2;
    public static final int COND_GE = 3;
    public static final int COND_GT = 4;
    public static final int COND_NULL = 5;


    // ========= CONST =========
    public static final int CONST = 0x00;

    // ========= Arithmetic =========
    public static final int ADD = 0x01;
    public static final int SUB = 0x02;
    public static final int MUL = 0x03;
    public static final int DIV = 0x04;
    public static final int NEG = 0x05;
    public static final int POW = 0x06;

    // ========= Flow Ops =========
    public static final int GOTO = 0x07;
    public static final int ICOND = 0x08;
    public static final int FCOND = 0x09;
    public static final int LCOND = 0x0A;
    public static final int DCOND = 0x0B;
    public static final int SCOND = 0x0C;
    public static final int OCOND = 0x0D;
    public static final int RETURN = 0x0E;

    // ========= Calls =========
    public static final int CALL = 0x10;
    public static final int VCALL = 0x11;
    public static final int DCALL = 0x12; // Dynamic calls - parameter counts in registers instead of immediates
    public static final int DVCALL = 0x13;

    // ========= Bitwise Ops =========
    public static final int BAND = 0x14;
    public static final int BOR = 0x15;
    public static final int BXOR = 0x16;
    public static final int BNOT = 0x17;
    public static final int BLSHIFT = 0x18;
    public static final int BSRSHIFT = 0x19;
    public static final int BURSHIFT = 0x1A;

    // ========= Conversion Ops =========
    public static final int CONV = 0x1B;

    // ========= Memory Ops =========
    public static final int WRITE = 0x1C;
    public static final int READ = 0x1D;
    public static final int MWRITE = 0x1E;
    public static final int MREAD = 0x1F;
    public static final int GWRITE = 0x20;
    public static final int GREAD = 0x21;
    public static final int ALLOCATED = 0x22;
    public static final int STRINGS = 0x23;
    public static final int OBJECTS = 0x24;
    public static final int RESIZE = 0x25;

    // ========= String Ops =========
    public static final int CHAR_AT = 0x26;
    public static final int STR_LEN = 0x27;
    public static final int CHARS = 0x28;
    public static final int STR = 0x29;
    public static final int CONCAT = 0x2A;
    public static final int SUB_STR = 0x2B;

    // ========= Frame Inspection Ops =========
    public static final int PARENT = 0x2C;
    public static final int REG_COUNT = 0x2D;
    public static final int REG_COPY = 0x2E;
    public static final int IP = 0x2F;

    // ========= Fiber Ops =========
    public static final int LAUNCH = 0x30;
    public static final int VLAUNCH = 0x31;
    public static final int YIELD = 0x32;
    public static final int VYIELD = 0x33;
    public static final int BLOCK = 0x34;
    public static final int UNBLOCK = 0x35;

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
        return new Opcode(ICOND, dst, Opcodes.encodeRegisterOperand(COND_LT, src), target);
    }

    @Assembles("IFLE")
    public static Opcode IFLE(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(ICOND, dst, Opcodes.encodeRegisterOperand(COND_LE, src), target);
    }

    @Assembles("IFEQ")
    public static Opcode IFEQ(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(ICOND, dst, Opcodes.encodeRegisterOperand(COND_EQ, src), target);
    }

    @Assembles("IFGE")
    public static Opcode IFGE(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(ICOND, dst, Opcodes.encodeRegisterOperand(COND_GE, src), target);
    }

    @Assembles("IFGT")
    public static Opcode IFGT(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(ICOND, dst, Opcodes.encodeRegisterOperand(COND_GT, src), target);
    }

    @Assembles("RETURN")
    public static Opcode RETURN(@Register Integer src){
        return new Opcode(RETURN, src);
    }

    public static int registerType(int reg){
        return (reg >> 24) & 0xFF;
    }

    public static int registerIndex(int reg){
        return 0x00FFFFFF & reg;
    }

    public static int encodeRegisterOperand(TypeFlag type, int reg){
        return (type.ordinal() << 24) | reg;
    }

    public static int encodeRegisterOperand(int flags, int reg){
        return (flags << 24) | reg;
    }

    public static int encodeOpcodeType(int type, int opcode){
        return (type << 24) | opcode;
    }
}
