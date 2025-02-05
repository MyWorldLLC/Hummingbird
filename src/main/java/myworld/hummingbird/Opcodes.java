package myworld.hummingbird;

import myworld.hummingbird.instructions.*;
import myworld.hummingbird.instructions.arithmetic.*;
import myworld.hummingbird.instructions.bitwise.*;
import myworld.hummingbird.instructions.flow.*;
import myworld.hummingbird.instructions.memory.*;
import myworld.hummingbird.instructions.string.*;

@SuppressWarnings("unused")
public class Opcodes {

    public static final int COND_LT = 0;
    public static final int COND_LE = 1;
    public static final int COND_EQ = 2;
    public static final int COND_NE = 3;
    public static final int COND_GE = 4;
    public static final int COND_GT = 5;
    public static final int COND_NULL = 6;

    // ========= CONST =========
    public static final int CONST = 0x00;
    public static final int NULL = 0x01;

    // ========= Arithmetic =========
    public static final int ADD = 0x02;
    public static final int SUB = 0x03;
    public static final int MUL = 0x04;
    public static final int DIV = 0x05;
    public static final int REM = 0x06;
    public static final int NEG = 0x07;
    public static final int POW = 0x08;
    public static final int ABS = 0x09;
    public static final int CADD = 0x0A;

    // ========= Bitwise =========
    public static final int BAND = 0x0B;
    public static final int BOR = 0x0C;
    public static final int BXOR = 0x0D;
    public static final int BNOT = 0x0E;
    public static final int BLSHIFT = 0x0F;
    public static final int BSRSHIFT = 0x10;
    public static final int BURSHIFT = 0x11;

    // ========= Conversion =========
    public static final int L2D = 0x12;
    public static final int D2L = 0x13;

    // ========= Flow =========
    public static final int GOTO = 0x14;
    public static final int JMP = 0x15;
    public static final int ICOND = 0x16;
    public static final int DCOND = 0x17;

    // ========= Registers =========

    public static final int PARAM = 0x18;
    public static final int PARAMS = 0x19;
    public static final int COPY = 0x1A;
    public static final int SAVE = 0x1B;
    public static final int RESTORE = 0x1C;
    public static final int IP = 0x1D;

    // ========= Calls =========
    public static final int CALL = 0x1E;
    public static final int DCALL = 0x1F;
    public static final int FCALL = 0x20;
    public static final int DFCALL = 0x21;
    public static final int RETURN = 0x22;

    // ========= Fibers =========
    public static final int SPAWN = 0x23;
    public static final int YIELD = 0x24;
    public static final int BLOCK = 0x25;
    public static final int UNBLOCK = 0x26;

    // ========= Memory =========
    public static final int WRITE = 0x27;
    public static final int READ = 0x28;
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

    // ========= Exceptions =========
    public static final int TRAPS = 0x36;
    public static final int TRAP = 0x37;

    // ========= Debug =========
    public static final int DEBUG = 0x38;

    @Assembles("CONST")
    public static Opcode CONST(@Register Integer dst, @Immediate Object src){
        if(src instanceof Integer i) {
            return new Opcode(CONST, dst, 0, i, 0, new ConstImpl(i));
        }else if(src instanceof Float f){
            return new Opcode(CONST, dst, Float.floatToIntBits(f), 0, 0, new ConstImpl(Float.floatToIntBits(f)));
        }else if(src instanceof Long l){
            return new Opcode(CONST, dst, highBits(l), lowBits(l), 0, new ConstImpl(l));
        } else if(src instanceof Double d){
            var l = Double.doubleToLongBits(d);
            return new Opcode(CONST, dst, highBits(l), lowBits(l), 0, new ConstImpl(l));
        }
        throw new IllegalArgumentException("Invalid operand type: " + src.getClass());
    }

    @Assembles("NULL")
    public static Opcode NULL(@Register Integer dst){
        return new Opcode(NULL, dst, new NullImpl());
    }

    @Assembles("ADD")
    public static Opcode ADD(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(ADD, dst, a, b, new AddImpl());
    }

    @Assembles("SUB")
    public static Opcode SUB(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(SUB, dst, a, b, new SubImpl());
    }

    @Assembles("MUL")
    public static Opcode MUL(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(MUL, dst, a, b, new MulImpl());
    }

    @Assembles("DIV")
    public static Opcode DIV(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(DIV, dst, a, b, new DivImpl());
    }

    @Assembles("REM")
    public static Opcode REM(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(REM, dst, a, b, new RemImpl());
    }

    @Assembles("NEG")
    public static Opcode NEG(@Register Integer dst, @Register Integer a){
        return new Opcode(NEG, dst, a, new NegImpl());
    }

    @Assembles("POW")
    public static Opcode POW(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(POW, dst, a, b, new PowImpl());
    }

    @Assembles("ABS")
    public static Opcode ABS(@Register Integer dst, @Register Integer a){
        return new Opcode(ABS, dst, a, new AbsImpl());
    }

    @Assembles("CADD")
    public static Opcode CADD(@Register Integer dst, @Register Integer a, @Immediate Long value){
        return new Opcode(CADD, dst, a, new CAddImpl(value));
    }

    @Assembles("DADD")
    public static Opcode DADD(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(ADD, dst, a, b, new DAddImpl());
    }

    @Assembles("DSUB")
    public static Opcode DSUB(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(SUB, dst, a, b, new DSubImpl());
    }

    @Assembles("DMUL")
    public static Opcode DMUL(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(MUL, dst, a, b, new DMulImpl());
    }

    @Assembles("DDIV")
    public static Opcode DDIV(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(DIV, dst, a, b, new DDivImpl());
    }

    @Assembles("DREM")
    public static Opcode DREM(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(REM, dst, a, b, new DRemImpl());
    }

    @Assembles("DNEG")
    public static Opcode DNEG(@Register Integer dst, @Register Integer a){
        return new Opcode(NEG, dst, a, new DNegImpl());
    }

    @Assembles("DPOW")
    public static Opcode DPOW(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(POW, dst, a, b, new DPowImpl());
    }

    @Assembles("DABS")
    public static Opcode DABS(@Register Integer dst, @Register Integer a){
        return new Opcode(ABS, dst, a, new DAbsImpl());
    }

    @Assembles("DCADD")
    public static Opcode DCADD(@Register Integer dst, @Register Integer a, @Immediate Double value){
        return new Opcode(CADD, dst, a, new DCAddImpl(value));
    }

    @Assembles("BAND")
    public static Opcode BAND(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(BAND, dst, a, b, new BandImpl());
    }

    @Assembles("BOR")
    public static Opcode BOR(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(BOR, dst, a, b, new BorImpl());
    }

    @Assembles("BXOR")
    public static Opcode BXOR(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(BXOR, dst, a, b, new BxorImpl());
    }

    @Assembles("BNOT")
    public static Opcode BNOT(@Register Integer dst, @Register Integer a){
        return new Opcode(BNOT, dst, a, new BnotImpl());
    }

    @Assembles("BLSHIFT")
    public static Opcode BLSHIFT(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(BLSHIFT, dst, a, b, new BlshiftImpl());
    }

    @Assembles("BSRSHIFT")
    public static Opcode BSRSHIFT(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(BSRSHIFT, dst, a, b, new BrshiftImpl());
    }

    @Assembles("BURSHIFT")
    public static Opcode BURSHIFT(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(BURSHIFT, dst, a, b, new BurshiftImpl());
    }

    @Assembles("GOTO")
    public static Opcode GOTO(@Immediate Integer dst){
        return new Opcode(GOTO, dst, new GotoImpl());
    }

    @Assembles("JMP")
    public static Opcode JMP(@Register Integer dst){
        return new Opcode(JMP, dst, new JmpImpl());
    }

    @Assembles("IFLT")
    public static Opcode IFLT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(ICOND, dst, src, COND_LT, target, new ICondImpl());
    }

    @Assembles("IFLE")
    public static Opcode IFLE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(ICOND, dst, src, COND_LE, target, new ICondImpl());
    }

    @Assembles("IFEQ")
    public static Opcode IFEQ(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(ICOND, dst, src, COND_EQ, target, new ICondImpl());
    }

    @Assembles("IFNE")
    public static Opcode IFNE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(ICOND, dst, src, COND_NE, target, new ICondImpl());
    }

    @Assembles("IFGE")
    public static Opcode IFGE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(ICOND, dst, src, COND_GE, target, new ICondImpl());
    }

    @Assembles("IFGT")
    public static Opcode IFGT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(ICOND, dst, src, COND_GT, target, new ICondImpl());
    }

    @Assembles("IFNULL")
    public static Opcode IFNULL(@Register Integer dst, @Immediate Integer target){
        return new Opcode(COND_NULL, dst, target, new IfNullImpl());
    }

    @Assembles("TIFLT")
    public static Opcode TIFLT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(ICOND, dst, src, COND_LT, target, new ICondImpl(false));
    }

    @Assembles("TIFLE")
    public static Opcode TIFLE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(ICOND, dst, src, COND_LE, target, new ICondImpl(false));
    }

    @Assembles("TIFEQ")
    public static Opcode TIFEQ(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(ICOND, dst, src, COND_EQ, target, new ICondImpl(false));
    }

    @Assembles("TIFNE")
    public static Opcode TIFNE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(ICOND, dst, src, COND_NE, target, new ICondImpl(false));
    }

    @Assembles("TIFGE")
    public static Opcode TIFGE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(ICOND, dst, src, COND_GE, target, new ICondImpl(false));
    }

    @Assembles("TIFGT")
    public static Opcode TIFGT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(ICOND, dst, src, COND_GT, target, new ICondImpl(false));
    }

    @Assembles("TIFNULL")
    public static Opcode TIFNULL(@Register Integer dst, @Immediate Integer target){
        return new Opcode(COND_NULL, dst, target, new IfNullImpl(false));
    }

    @Assembles("RETURN")
    public static Opcode RETURN(@Register Integer value){
        return new Opcode(RETURN, value, 0, 0, 0, new ReturnImpl());
    }

    @Assembles("MRETURN")
    public static Opcode MRETURN(@Register Integer value, @Immediate Integer count){
        return new Opcode(RETURN, value, count, 0, 0, new MReturnImpl());
    }

    @Assembles("COPY")
    public static Opcode COPY(@Register Integer dst, @Register Integer src) {
        return new Opcode(COPY, dst, src, new CopyImpl());
    }

    @Assembles("L2D")
    public static Opcode L2D(@Register Integer dst, @Register Integer src) {
        return new Opcode(L2D, dst, src, new L2DImpl());
    }

    @Assembles("D2L")
    public static Opcode D2L(@Register Integer dst, @Register Integer src) {
        return new Opcode(D2L, dst, src, new D2LImpl());
    }

    @Assembles("DIFLT")
    public static Opcode DIFLT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(DCOND, dst, src, COND_LT, target, new DCondImpl());
    }

    @Assembles("DIFLE")
    public static Opcode DIFLE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(DCOND, dst, src, COND_LE, target, new DCondImpl());
    }

    @Assembles("DIFEQ")
    public static Opcode DIFEQ(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(DCOND, dst, src, COND_EQ, target, new DCondImpl());
    }

    @Assembles("DIFNE")
    public static Opcode DIFNE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(DCOND, dst, src, COND_NE, target, new DCondImpl());
    }

    @Assembles("DIFGE")
    public static Opcode DIFGE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(DCOND, dst, src, COND_GE, target, new DCondImpl());
    }

    @Assembles("DIFGT")
    public static Opcode DIFGT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(DCOND, dst, src, COND_GT, target, new DCondImpl());
    }

    @Assembles("TDIFLT")
    public static Opcode TDIFLT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(DCOND, dst, src, COND_LT, target, new DCondImpl(false));
    }

    @Assembles("TDIFLE")
    public static Opcode TDIFLE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(DCOND, dst, src, COND_LE, target, new DCondImpl(false));
    }

    @Assembles("TDIFEQ")
    public static Opcode TDIFEQ(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(DCOND, dst, src, COND_EQ, target, new DCondImpl(false));
    }

    @Assembles("TDIFNE")
    public static Opcode TDIFNE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(DCOND, dst, src, COND_NE, target, new DCondImpl(false));
    }

    @Assembles("TDIFGE")
    public static Opcode TDIFGE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(DCOND, dst, src, COND_GE, target, new DCondImpl(false));
    }

    @Assembles("TDIFGT")
    public static Opcode TDIFGT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(DCOND, dst, src, COND_GT, target, new DCondImpl(false));
    }

    @Assembles("IP")
    public static Opcode IP(@Register Integer dst) {
        return new Opcode(IP, dst, new IpImpl());
    }

    @Assembles("CALL0")
    public static Opcode CALL0(@Register Integer dst, @Immediate Integer symbol) {
        return new Opcode(CALL, dst, symbol, new CallImpl());
    }

    @Assembles("CALL")
    public static Opcode CALL(@Register Integer dst, @Immediate Integer symbol, @Register Integer src, @Immediate Integer count) {
        return new Opcode(CALL, dst, symbol, src, count, new CallImpl());
    }

    @Assembles("DCALL0")
    public static Opcode DCALL0(@Register Integer dst, @Immediate Integer symbol) {
        return new Opcode(DCALL, dst, symbol, new DCallImpl());
    }

    @Assembles("DCALL")
    public static Opcode DCALL(@Register Integer dst, @Immediate Integer symbol, @Register Integer src, @Immediate Integer count) {
        return new Opcode(DCALL, dst, symbol, src, count, new DCallImpl());
    }

    @Assembles("FCALL0")
    public static Opcode FCALL0(@Immediate Integer symbol) {
        return new Opcode(FCALL, symbol, new FCallImpl());
    }

    @Assembles("FCALL")
    public static Opcode FCALL(@Register Integer dst, @Immediate Integer symbol, @Register Integer src, @Immediate Integer count) {
        return new Opcode(FCALL, dst, symbol, src, count, new FCallImpl());
    }

    @Assembles("DFCALL0")
    public static Opcode DFCALL0(@Immediate Integer symbol) {
        return new Opcode(DFCALL, symbol, new DFCallImpl());
    }

    @Assembles("DFCALL")
    public static Opcode DFCALL(@Register Integer dst, @Immediate Integer symbol, @Register Integer src, @Immediate Integer count) {
        return new Opcode(DFCALL, dst, symbol, src, count, new DFCallImpl());
    }

    @Assembles("SPAWN")
    public static Opcode SPAWN(@Register Integer dst, @Immediate Integer src){
        return new Opcode(SPAWN, dst, src, new SpawnImpl());
    }

    @Assembles("YIELD")
    public static Opcode YIELD(){
        return new Opcode(YIELD, new YieldImpl());
    }

    @Assembles("BLOCK")
    public static Opcode BLOCK(){
        return new Opcode(BLOCK, new BlockImpl());
    }

    @Assembles("UNBLOCK")
    public static Opcode UNBLOCK(@Register Integer dst){
        return new Opcode(UNBLOCK, dst, new UnblockImpl());
    }

    @Assembles("WRITE")
    public static Opcode WRITE(@Register Integer dst, @Register Integer src, @Immediate Integer size) {
        return new Opcode(WRITE, dst, src, size, new WriteImpl());
    }

    @Assembles("READ")
    public static Opcode READ(@Register Integer dst, @Register Integer src, @Immediate Integer size) {
        return new Opcode(READ, dst, src, size, new ReadImpl());
    }

    @Assembles("FWRITE")
    public static Opcode FWRITE(@Register Integer dst, @Register Integer src, @Immediate Integer size, @Immediate Integer offset) {
        return new Opcode(WRITE, dst, src, size, offset, new WriteImpl());
    }

    @Assembles("FREAD")
    public static Opcode FREAD(@Register Integer dst, @Register Integer src, @Immediate Integer size, @Immediate Integer offset) {
        return new Opcode(READ, dst, src, size, offset, new ReadImpl());
    }

    @Assembles("MEM_COPY")
    public static Opcode MEM_COPY(@Register Integer dst, @Register Integer src, @Register Integer end) {
        return new Opcode(MEM_COPY, dst, src, end, new MemCopyImpl());
    }

    @Assembles("OBJ_COPY")
    public static Opcode OBJ_COPY(@Register Integer dst, @Register Integer src, @Register Integer end) {
        return new Opcode(OBJ_COPY, dst, src, end, new ObjCopyImpl());
    }

    @Assembles("ALLOCATED")
    public static Opcode ALLOCATED(@Register Integer dst) {
        return new Opcode(ALLOCATED, dst, new AllocatedImpl());
    }

    @Assembles("OBJ_ALLOCATED")
    public static Opcode OBJ_ALLOCATED(@Register Integer dst) {
        return new Opcode(ALLOCATED, dst, new ObjAllocatedImpl());
    }

    @Assembles("RESIZE")
    public static Opcode RESIZE(@Register Integer dst, @Register Integer size) {
        return new Opcode(RESIZE, dst, size, new ResizeImpl());
    }

    @Assembles("OBJ_RESIZE")
    public static Opcode OBJ_RESIZE(@Register Integer dst, @Register Integer size) {
        return new Opcode(OBJ_RESIZE, dst, size, new ObjResizeImpl());
    }

    @Assembles("STR")
    public static Opcode STR(@Register Integer dst, @Register Integer src, @Immediate Integer type) {
        return new Opcode(STR, dst, src, type, new StrImpl());
    }

    @Assembles("STR_LEN")
    public static Opcode STR_LEN(@Register Integer dst, @Register Integer src) {
        return new Opcode(STR_LEN, dst, src, new StrLenImpl());
    }

    @Assembles("CHAR_AT")
    public static Opcode CHAR_AT(@Register Integer dst, @Register Integer src, @Register Integer index) {
        return new Opcode(CHAR_AT, dst, src, index, new CharAtImpl());
    }

    @Assembles("TO_CHARS")
    public static Opcode TO_CHARS(@Register Integer dst, @Register Integer src) {
        return new Opcode(TO_CHARS, dst, src, new ToCharsImpl());
    }

    @Assembles("FROM_CHARS")
    public static Opcode FROM_CHARS(@Register Integer dst, @Register Integer src) {
        return new Opcode(FROM_CHARS, dst, src, new FromCharsImpl());
    }

    @Assembles("CONCAT")
    public static Opcode CONCAT(@Register Integer dst, @Register Integer src) {
        return new Opcode(CONCAT, dst, src, new ConcatImpl());
    }

    @Assembles("SUB_STR")
    public static Opcode SUB_STR(@Register Integer dst, @Register Integer src, @Register Integer start, @Register Integer end) {
        return new Opcode(SUB_STR, dst, src, start, end, new SubStrImpl());
    }

    @Assembles("SCOMP")
    public static Opcode SCOMP(@Register Integer dst, @Register Integer a, @Register Integer b) {
        return new Opcode(SCOMP, dst, a, b, new SCompImpl());
    }

    @Assembles("TRAPS")
    public static Opcode TRAPS(@Immediate Integer tableAddr, @Immediate Integer handlerCount) {
        return new Opcode(TRAPS, tableAddr, handlerCount, new TrapsImpl());
    }

    @Assembles("TRAP")
    public static Opcode TRAP(@Register Integer code) {
        return new Opcode(TRAP, code, new TrapImpl());
    }

    @Assembles("PARAMS")
    public static Opcode PARAMS(@Register Integer dst, @Immediate Integer pIndex, @Immediate Integer count){
        return new Opcode(PARAMS, dst, pIndex, count, new ParamsImpl());
    }

    @Assembles("DEBUG")
    public static Opcode DEBUG(@Immediate Integer sentinel, @Register Integer src){
        return new Opcode(DEBUG, sentinel, src, new DebugImpl());
    }

    public static int highBits(long l){
        return (int) (l >>> 32);
    }

    public static int lowBits(long l){
        return (int) l;
    }

    public static int opcode(long opcode){
        return (int)((opcode >> 56) & 0xFF);
    }

}
