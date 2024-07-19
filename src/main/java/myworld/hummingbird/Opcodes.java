package myworld.hummingbird;

public class Opcodes {

    public static final int BYTE_T = -3;
    public static final int CHAR_T = -2;
    public static final int SHORT_T = -1;
    public static final int INT_T = 0;
    public static final int FLOAT_T = 1;
    public static final int LONG_T = 2;
    public static final int DOUBLE_T = 3;
    public static final int OBJECT_T = 4;

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
    public static final int OCOND = 0x15;

    // ========= Registers =========

    public static final int COPY = 0x16;
    public static final int SAVE = 0x17;
    public static final int RESTORE = 0x18;
    public static final int IP = 0x19;

    // ========= Calls =========
    public static final int CALL = 0x1A;
    public static final int DCALL = 0x1B;
    public static final int FCALL = 0x1C;
    public static final int DFCALL = 0x1D;
    public static final int RETURN = 0x1E;

    // ========= Fibers =========
    public static final int SPAWN = 0x1F;
    public static final int YIELD = 0x20;
    public static final int BLOCK = 0x21;
    public static final int UNBLOCK = 0x22;

    // ========= Memory =========
    public static final int WRITE = 0x23;
    public static final int READ = 0x24;
    public static final int SWRITE = 0x25;
    public static final int SREAD = 0x26;
    public static final int GWRITE = 0x27;
    public static final int GREAD = 0x28;
    public static final int MEM_COPY = 0x29;
    public static final int OBJ_COPY = 0x2A;
    public static final int ALLOCATED = 0x2B;
    public static final int RESIZE = 0x2C;
    public static final int OBJ_RESIZE = 0x2D;

    // ========= Strings =========
    public static final int STR = 0x2E;
    public static final int STR_LEN = 0x2F;
    public static final int CHAR_AT = 0x30;
    public static final int TO_CHARS = 0x31;
    public static final int FROM_CHARS = 0x32;
    public static final int CONCAT = 0x33;
    public static final int SUB_STR = 0x34;
    public static final int SCOMP = 0x35;

    @Assembles("CONST")
    public static Opcode CONST(@Register Integer dst, @Immediate Object src){
        if(src instanceof Integer i) {
            return new Opcode(CONST, dst, i);
        }else if(src instanceof Float f){
            return new Opcode(CONST, dst, Float.floatToIntBits(f));
        }else if(src instanceof Long l){
            return new Opcode(CONST, dst, highBits(l), lowBits(l));
        } else if(src instanceof Double d){
            var l = Double.doubleToLongBits(d);
            return new Opcode(CONST, dst, highBits(l), lowBits(l));
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

    @Assembles("MUL")
    public static Opcode MUL(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(MUL, dst, Opcodes.registerIndex(a), Opcodes.registerIndex(b));
    }

    @Assembles("DIV")
    public static Opcode DIV(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(DIV, dst, Opcodes.registerIndex(a), Opcodes.registerIndex(b));
    }

    @Assembles("NEG")
    public static Opcode NEG(@Register Integer dst, @Register Integer a){
        return new Opcode(NEG, dst, Opcodes.registerIndex(a));
    }

    @Assembles("POW")
    public static Opcode POW(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(POW, dst, Opcodes.registerIndex(a), Opcodes.registerIndex(b));
    }

    @Assembles("BAND")
    public static Opcode BAND(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(BAND, dst, Opcodes.registerIndex(a), Opcodes.registerIndex(b));
    }

    @Assembles("BOR")
    public static Opcode BOR(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(BOR, dst, Opcodes.registerIndex(a), Opcodes.registerIndex(b));
    }

    @Assembles("BXOR")
    public static Opcode BXOR(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(BXOR, dst, Opcodes.registerIndex(a), Opcodes.registerIndex(b));
    }

    @Assembles("BNOT")
    public static Opcode BNOT(@Register Integer dst, @Register Integer a){
        return new Opcode(BNOT, dst, Opcodes.registerIndex(a));
    }

    @Assembles("BLSHIFT")
    public static Opcode BLSHIFT(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(BLSHIFT, dst, Opcodes.registerIndex(a), Opcodes.registerIndex(b));
    }

    @Assembles("BSRSHIFT")
    public static Opcode BSRSHIFT(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(BSRSHIFT, dst, Opcodes.registerIndex(a), Opcodes.registerIndex(b));
    }

    @Assembles("BURSHIFT")
    public static Opcode BURSHIFT(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(BURSHIFT, dst, Opcodes.registerIndex(a), Opcodes.registerIndex(b));
    }

    @Assembles("CONV")
    public static Opcode CONV(@Register Integer dst, @Register Integer src){
        return new Opcode(CONV, dst, src);
    }

    @Assembles("GOTO")
    public static Opcode GOTO(@Immediate Integer dst){
        return new Opcode(GOTO, dst);
    }

    @Assembles("JMP")
    public static Opcode JMP(@Register Integer dst){
        return new Opcode(JMP, Opcodes.registerIndex(dst));
    }

    @Assembles("IFLT")
    public static Opcode IFLT(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(conditionType(dst), registerIndex(dst), Opcodes.encodeRegisterOperand(COND_LT, Opcodes.registerIndex(src)), target);
    }

    @Assembles("IFLE")
    public static Opcode IFLE(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(conditionType(dst), registerIndex(dst), Opcodes.encodeRegisterOperand(COND_LE, Opcodes.registerIndex(src)), target);
    }

    @Assembles("IFEQ")
    public static Opcode IFEQ(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(conditionType(dst), registerIndex(dst), Opcodes.encodeRegisterOperand(COND_EQ, Opcodes.registerIndex(src)), target);
    }

    @Assembles("IFGE")
    public static Opcode IFGE(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(conditionType(dst), registerIndex(dst), Opcodes.encodeRegisterOperand(COND_GE, Opcodes.registerIndex(src)), target);
    }

    @Assembles("IFGT")
    public static Opcode IFGT(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(conditionType(dst), registerIndex(dst), Opcodes.encodeRegisterOperand(COND_GT, Opcodes.registerIndex(src)), target);
    }

    @Assembles("IFNULL")
    public static Opcode IFNULL(@Register Integer dst, @Register Integer src, @Register Integer target){
        return new Opcode(conditionType(dst), registerIndex(dst), Opcodes.encodeRegisterOperand(COND_NULL, Opcodes.registerIndex(src)), target);
    }

    @Assembles("RETURN")
    public static Opcode RETURN(){
        return new Opcode(RETURN, 0);
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

    @Assembles("IP")
    public static Opcode IP(@Register Integer dst) {
        return new Opcode(IP, registerIndex(dst));
    }

    @Assembles("CALL")
    public static Opcode CALL(@Immediate Integer label) {
        return new Opcode(CALL, label);
    }

    @Assembles("DCALL")
    public static Opcode DCALL(@Register Integer dst) {
        return new Opcode(DCALL, registerIndex(dst));
    }

    @Assembles("FCALL")
    public static Opcode FCALL(@Immediate Integer symbol) {
        return new Opcode(FCALL, symbol);
    }

    @Assembles("DFCALL")
    public static Opcode DFCALL(@Register Integer dst) {
        return new Opcode(DFCALL, registerIndex(dst));
    }

    @Assembles("SPAWN")
    public static Opcode SPAWN(@Register Integer dst, @Immediate Integer src){
        return new Opcode(SPAWN, dst, src);
    }

    @Assembles("YIELD")
    public static Opcode YIELD(){
        return new Opcode(YIELD);
    }

    @Assembles("BLOCK")
    public static Opcode BLOCK(){
        return new Opcode(BLOCK);
    }

    @Assembles("UNBLOCK")
    public static Opcode UNBLOCK(@Register Integer dst){
        return new Opcode(UNBLOCK, dst);
    }

    @Assembles("WRITE")
    public static Opcode WRITE(@Register Integer dst, @Register Integer src) {
        return new Opcode(WRITE, dst, src);
    }

    @Assembles("READ")
    public static Opcode READ(@Register Integer dst, @Register Integer src) {
        return new Opcode(READ, dst, registerIndex(src));
    }

    @Assembles("SWRITE")
    public static Opcode SWRITE(@Register Integer dst, @Register Integer src, @Immediate Integer type) {
        return new Opcode(SWRITE, dst, src, type);
    }

    @Assembles("SREAD")
    public static Opcode SREAD(@Register Integer dst, @Register Integer src, @Immediate Integer type) {
        return new Opcode(SREAD, dst, registerIndex(src), type);
    }

    @Assembles("GWRITE")
    public static Opcode GWRITE(@Register Integer dst, @Register Integer src, @Register Integer guard, @Immediate Integer sentinel) {
        return new Opcode(GWRITE, dst, src, guard, sentinel.byteValue());
    }

    @Assembles("GREAD")
    public static Opcode GREAD(@Register Integer dst, @Register Integer src, @Register Integer guard) {
        return new Opcode(GREAD, dst, registerIndex(src), registerIndex(guard));
    }

    @Assembles("MEM_COPY")
    public static Opcode MEM_COPY(@Register Integer dst, @Register Integer src, @Register Integer end) {
        return new Opcode(MEM_COPY, registerIndex(dst), registerIndex(src), registerIndex(end));
    }

    @Assembles("OBJ_COPY")
    public static Opcode OBJ_COPY(@Register Integer dst, @Register Integer src, @Register Integer end) {
        return new Opcode(OBJ_COPY, registerIndex(dst), registerIndex(src), registerIndex(end));
    }

    @Assembles("ALLOCATED")
    public static Opcode ALLOCATED(@Register Integer dst, @Immediate Integer type) {
        return new Opcode(ALLOCATED, registerIndex(dst), type);
    }

    @Assembles("RESIZE")
    public static Opcode RESIZE(@Register Integer dst) {
        return new Opcode(RESIZE, registerIndex(dst));
    }

    @Assembles("OBJ_RESIZE")
    public static Opcode OBJ_RESIZE(@Register Integer dst) {
        return new Opcode(OBJ_RESIZE, registerIndex(dst));
    }

    @Assembles("STR")
    public static Opcode STR(@Register Integer dst, @Register Integer src) {
        return new Opcode(STR, registerIndex(dst), src);
    }

    @Assembles("STR_LEN")
    public static Opcode STR_LEN(@Register Integer dst, @Register Integer src) {
        return new Opcode(STR_LEN, registerIndex(dst), registerIndex(src));
    }

    @Assembles("CHAR_AT")
    public static Opcode CHAR_AT(@Register Integer dst, @Register Integer src, @Register Integer extra) {
        return new Opcode(CHAR_AT, registerIndex(dst), registerIndex(src), registerIndex(extra));
    }

    @Assembles("TO_CHARS")
    public static Opcode TO_CHARS(@Register Integer dst, @Register Integer src) {
        return new Opcode(TO_CHARS, registerIndex(dst), registerIndex(src));
    }

    @Assembles("FROM_CHARS")
    public static Opcode FROM_CHARS(@Register Integer dst, @Register Integer src) {
        return new Opcode(FROM_CHARS, registerIndex(dst), registerIndex(src));
    }

    @Assembles("CONCAT")
    public static Opcode CONCAT(@Register Integer dst, @Register Integer src) {
        return new Opcode(CONCAT, registerIndex(dst), registerIndex(src));
    }

    @Assembles("SUB_STR")
    public static Opcode SUB_STR(@Register Integer dst, @Register Integer src, @Register Integer start, @Register Integer end) {
        return new Opcode(SUB_STR, registerIndex(dst), registerIndex(src), registerIndex(start), registerIndex(end));
    }

    @Assembles("SCOMP")
    public static Opcode SCOMP(@Register Integer dst, @Register Integer a, @Register Integer b) {
        return new Opcode(SCOMP, registerIndex(dst), registerIndex(a), registerIndex(b));
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

    public static int highBits(long l){
        return (int) (l >>> 32);
    }

    public static int lowBits(long l){
        return (int) l;
    }

    private static int conditionType(int dstReg){
        return switch (registerType(dstReg)){
            case INT_T -> ICOND;
            case FLOAT_T -> FCOND;
            case LONG_T -> LCOND;
            case DOUBLE_T -> DCOND;
            case OBJECT_T -> OCOND;
            default -> 0;
        };
    }
}
