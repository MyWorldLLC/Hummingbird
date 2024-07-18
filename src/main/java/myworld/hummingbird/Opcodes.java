package myworld.hummingbird;

public class Opcodes {

    public static final int INT_T = 0;
    public static final int FLOAT_T = 1;
    public static final int LONG_T = 2;
    public static final int DOUBLE_T = 3;
    public static final int STRING_T = 4;
    public static final int OBJECT_T = 5;

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

    // ========= Bitwise =========
    public static final int BAND = 0x07;
    public static final int BOR = 0x08;
    public static final int BXOR = 0x09;
    public static final int BNOT = 0x0A;
    public static final int BLSHIFT = 0x0B;
    public static final int BSRSHIFT = 0x0C;
    public static final int BURSHIFT = 0x0D;

    // ========= Conversion =========
    public static final int CONV = 0x0E;

    // ========= Flow =========
    public static final int GOTO = 0x0F;
    public static final int JMP = 0x10;
    public static final int ICOND = 0x11;
    public static final int FCOND = 0x12;
    public static final int LCOND = 0x13;
    public static final int DCOND = 0x14;
    public static final int SCOND = 0x15;
    public static final int OCOND = 0x16;

    // ========= Registers =========

    public static final int COPY = 0x17;
    public static final int SAVE = 0x18;
    public static final int RESTORE = 0x19;
    public static final int IP = 0x1A;

    // ========= Calls =========
    public static final int CALL = 0x1B;
    public static final int FCALL = 0x1C;
    public static final int RETURN = 0x1D;

    // ========= Fibers =========
    public static final int SPAWN = 0x1E;
    public static final int YIELD = 0x1F;
    public static final int BLOCK = 0x20;
    public static final int UNBLOCK = 0x21;

    // ========= Memory =========
    public static final int WRITE = 0x22;
    public static final int READ = 0x23;
    public static final int MWRITE = 0x24;
    public static final int MREAD = 0x25;
    public static final int GWRITE = 0x26;
    public static final int GREAD = 0x27;
    public static final int MEM_COPY = 0x28;
    public static final int OBJ_COPY = 0x29;
    public static final int ALLOCATED = 0x2A;
    public static final int RESIZE = 0x2B;
    public static final int OBJ_RESIZE = 0x2C;

    // ========= Strings =========
    public static final int STR = 0x2D;
    public static final int STR_LEN = 0x2E;
    public static final int CHAR_AT = 0x2F;
    public static final int TO_CHARS = 0x30;
    public static final int FROM_CHARS = 0x31;
    public static final int CONCAT = 0x32;
    public static final int SUB_STR = 0x33;

    @Assembles("CONST")
    public static Opcode CONST(@Register Integer dst, @Immediate Object src){
        if(src instanceof Integer i) {
            return new Opcode(CONST, dst, i);
        }else if(src instanceof Float f){
            return new Opcode(CONST, dst, Float.floatToIntBits(f));
        }
        throw new IllegalArgumentException("Invalid operand type: " + src.getClass());
    }

    @Assembles("ADD")
    public static Opcode ADD(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(ADD, dst, Opcodes.registerIndex(a), Opcodes.registerIndex(b));
    }

    @Assembles("SUB")
    public static Opcode SUB(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(SUB, dst, Opcodes.registerIndex(a), Opcodes.registerIndex(b));
    }

    @Assembles("DIV")
    public static Opcode DIV(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(DIV, dst, Opcodes.registerIndex(a), Opcodes.registerIndex(b));
    }

    @Assembles("IFLT")
    public static Opcode IFLT(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(ICOND, dst, Opcodes.encodeRegisterOperand(COND_LT, Opcodes.registerIndex(src)), target);
    }

    @Assembles("IFLE")
    public static Opcode IFLE(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(ICOND, dst, Opcodes.encodeRegisterOperand(COND_LE, Opcodes.registerIndex(src)), target);
    }

    @Assembles("IFEQ")
    public static Opcode IFEQ(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(ICOND, dst, Opcodes.encodeRegisterOperand(COND_EQ, Opcodes.registerIndex(src)), target);
    }

    @Assembles("IFGE")
    public static Opcode IFGE(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(ICOND, dst, Opcodes.encodeRegisterOperand(COND_GE, Opcodes.registerIndex(src)), target);
    }

    @Assembles("IFGT")
    public static Opcode IFGT(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(ICOND, dst, Opcodes.encodeRegisterOperand(COND_GT, Opcodes.registerIndex(src)), target);
    }

    @Assembles("RETURN")
    public static Opcode RETURN(){
        return new Opcode(RETURN, 0);
    }

    @Assembles("CALL")
    public static Opcode CALL(@Immediate Integer label) {
        return new Opcode(CALL, label);
    }

    @Assembles("COPY")
    public static Opcode COPY(@Register Integer dst, @Register Integer src) {
        return new Opcode(COPY, dst, Opcodes.registerIndex(src));
    }

    @Assembles("SAVE")
    public static Opcode SAVE(@Register Integer dst, @Immediate Integer count) {
        return new Opcode(SAVE, dst, count);
    }

    @Assembles("RESTORE")
    public static Opcode RESTORE(@Register Integer dst, @Immediate Integer count) {
        return new Opcode(RESTORE, dst, count);
    }

    @Assembles("SPAWN")
    public static Opcode SPAWN(@Register Integer dst, @Immediate Integer src){
        return new Opcode(SPAWN, dst, src);
    }

    @Assembles("YIELD")
    public static Opcode YIELD(){
        return new Opcode(YIELD, 0);
    }

    public static int registerType(int reg){
        return (reg >> 24) & 0xFF;
    }

    public static int registerIndex(int reg){
        return 0x00FFFFFF & reg;
    }

    public static int encodeRegisterOperand(TypeFlag type, int reg){
        return encodeOpcodeType(type.ordinal(), reg);
    }

    public static int encodeRegisterOperand(int flags, int reg){
        return (flags << 24) | reg;
    }

    public static int encodeOpcodeType(int type, int opcode){
        return (type << 24) | opcode;
    }
}
