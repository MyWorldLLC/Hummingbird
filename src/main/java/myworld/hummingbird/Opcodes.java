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


    @Assembles("CONST")
    public static Opcode CONST(@Register Integer dst, @Immediate Object src){
        if(src instanceof Integer i) {
            return new Opcode(new ConstImpl(i), dst, i);
        }else if(src instanceof Float f){
            return new Opcode(new ConstImpl(Float.floatToIntBits(f)), dst, Float.floatToIntBits(f));
        }else if(src instanceof Long l){
            return new Opcode(new ConstImpl(l), dst, highBits(l), lowBits(l), 0);
        } else if(src instanceof Double d){
            var l = Double.doubleToLongBits(d);
            return new Opcode(new ConstImpl(l), dst, highBits(l), lowBits(l), 0);
        }
        throw new IllegalArgumentException("Invalid operand type: " + src.getClass());
    }

    @Assembles("NULL")
    public static Opcode NULL(@Register Integer dst){
        return new Opcode(new NullImpl(), dst);
    }

    @Assembles("ADD")
    public static Opcode ADD(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new AddImpl(), dst, a, b);
    }

    @Assembles("SUB")
    public static Opcode SUB(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new SubImpl(), dst, a, b);
    }

    @Assembles("MUL")
    public static Opcode MUL(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new MulImpl(), dst, a, b);
    }

    @Assembles("DIV")
    public static Opcode DIV(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new DivImpl(), dst, a, b);
    }

    @Assembles("REM")
    public static Opcode REM(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new RemImpl(), dst, a, b);
    }

    @Assembles("NEG")
    public static Opcode NEG(@Register Integer dst, @Register Integer a){
        return new Opcode(new NegImpl(), dst, a);
    }

    @Assembles("POW")
    public static Opcode POW(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new PowImpl(), dst, a, b);
    }

    @Assembles("ABS")
    public static Opcode ABS(@Register Integer dst, @Register Integer a){
        return new Opcode(new AbsImpl(), dst, a);
    }

    @Assembles("CADD")
    public static Opcode CADD(@Register Integer dst, @Register Integer a, @Immediate Long value){
        return new Opcode(new CAddImpl(value), dst, a);
    }

    @Assembles("DADD")
    public static Opcode DADD(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new DAddImpl(), dst, a, b);
    }

    @Assembles("DSUB")
    public static Opcode DSUB(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new DSubImpl(), dst, a, b);
    }

    @Assembles("DMUL")
    public static Opcode DMUL(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new DMulImpl(), dst, a, b);
    }

    @Assembles("DDIV")
    public static Opcode DDIV(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new DDivImpl(), dst, a, b);
    }

    @Assembles("DREM")
    public static Opcode DREM(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new DRemImpl(), dst, a, b);
    }

    @Assembles("DNEG")
    public static Opcode DNEG(@Register Integer dst, @Register Integer a){
        return new Opcode(new DNegImpl(), dst, a);
    }

    @Assembles("DPOW")
    public static Opcode DPOW(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new DPowImpl(), dst, a, b);
    }

    @Assembles("DABS")
    public static Opcode DABS(@Register Integer dst, @Register Integer a){
        return new Opcode(new DAbsImpl(), dst, a);
    }

    @Assembles("DCADD")
    public static Opcode DCADD(@Register Integer dst, @Register Integer a, @Immediate Double value){
        return new Opcode(new DCAddImpl(value), dst, a);
    }

    @Assembles("BAND")
    public static Opcode BAND(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new BandImpl(), dst, a, b);
    }

    @Assembles("BOR")
    public static Opcode BOR(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new BorImpl(), dst, a, b);
    }

    @Assembles("BXOR")
    public static Opcode BXOR(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new BxorImpl(), dst, a, b);
    }

    @Assembles("BNOT")
    public static Opcode BNOT(@Register Integer dst, @Register Integer a){
        return new Opcode(new BnotImpl(), dst, a);
    }

    @Assembles("BLSHIFT")
    public static Opcode BLSHIFT(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new BlshiftImpl(), dst, a, b);
    }

    @Assembles("BSRSHIFT")
    public static Opcode BSRSHIFT(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new BrshiftImpl(), dst, a, b);
    }

    @Assembles("BURSHIFT")
    public static Opcode BURSHIFT(@Register Integer dst, @Register Integer a, @Register Integer b){
        return new Opcode(new BurshiftImpl(), dst, a, b);
    }

    @Assembles("GOTO")
    public static Opcode GOTO(@Immediate Integer dst){
        return new Opcode(new GotoImpl(), dst);
    }

    @Assembles("JMP")
    public static Opcode JMP(@Register Integer dst){
        return new Opcode(new JmpImpl(), dst);
    }

    @Assembles("IFLT")
    public static Opcode IFLT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new ICondImpl(), dst, src, COND_LT, target);
    }

    @Assembles("IFLE")
    public static Opcode IFLE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new ICondImpl(), dst, src, COND_LE, target);
    }

    @Assembles("IFEQ")
    public static Opcode IFEQ(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new ICondImpl(), dst, src, COND_EQ, target);
    }

    @Assembles("IFNE")
    public static Opcode IFNE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new ICondImpl(), dst, src, COND_NE, target);
    }

    @Assembles("IFGE")
    public static Opcode IFGE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new ICondImpl(), dst, src, COND_GE, target);
    }

    @Assembles("IFGT")
    public static Opcode IFGT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new ICondImpl(), dst, src, COND_GT, target);
    }

    @Assembles("IFNULL")
    public static Opcode IFNULL(@Register Integer dst, @Immediate Integer target){
        return new Opcode(new IfNullImpl(), COND_NULL, dst, target);
    }

    @Assembles("TIFLT")
    public static Opcode TIFLT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new ICondImpl(false), dst, src, COND_LT, target);
    }

    @Assembles("TIFLE")
    public static Opcode TIFLE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new ICondImpl(false), dst, src, COND_LE, target);
    }

    @Assembles("TIFEQ")
    public static Opcode TIFEQ(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new ICondImpl(false), dst, src, COND_EQ, target);
    }

    @Assembles("TIFNE")
    public static Opcode TIFNE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new ICondImpl(false), dst, src, COND_NE, target);
    }

    @Assembles("TIFGE")
    public static Opcode TIFGE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new ICondImpl(false), dst, src, COND_GE, target);
    }

    @Assembles("TIFGT")
    public static Opcode TIFGT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new ICondImpl(false), dst, src, COND_GT, target);
    }

    @Assembles("TIFNULL")
    public static Opcode TIFNULL(@Register Integer dst, @Immediate Integer target){
        return new Opcode(new IfNullImpl(false), COND_NULL, dst, target);
    }

    @Assembles("RETURN")
    public static Opcode RETURN(@Register Integer value){
        return new Opcode(new ReturnImpl(), value);
    }

    @Assembles("MRETURN")
    public static Opcode MRETURN(@Register Integer value, @Immediate Integer count){
        return new Opcode(new MReturnImpl(), value, count);
    }

    @Assembles("COPY")
    public static Opcode COPY(@Register Integer dst, @Register Integer src) {
        return new Opcode(new CopyImpl(), dst, src);
    }

    @Assembles("L2D")
    public static Opcode L2D(@Register Integer dst, @Register Integer src) {
        return new Opcode(new L2DImpl(), dst, src);
    }

    @Assembles("D2L")
    public static Opcode D2L(@Register Integer dst, @Register Integer src) {
        return new Opcode(new D2LImpl(), dst, src);
    }

    @Assembles("DIFLT")
    public static Opcode DIFLT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new DCondImpl(), dst, src, COND_LT, target);
    }

    @Assembles("DIFLE")
    public static Opcode DIFLE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new DCondImpl(), dst, src, COND_LE, target);
    }

    @Assembles("DIFEQ")
    public static Opcode DIFEQ(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new DCondImpl(), dst, src, COND_EQ, target);
    }

    @Assembles("DIFNE")
    public static Opcode DIFNE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new DCondImpl(), dst, src, COND_NE, target);
    }

    @Assembles("DIFGE")
    public static Opcode DIFGE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new DCondImpl(), dst, src, COND_GE, target);
    }

    @Assembles("DIFGT")
    public static Opcode DIFGT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new DCondImpl(), dst, src, COND_GT, target);
    }

    @Assembles("TDIFLT")
    public static Opcode TDIFLT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new DCondImpl(false), dst, src, COND_LT, target);
    }

    @Assembles("TDIFLE")
    public static Opcode TDIFLE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new DCondImpl(false), dst, src, COND_LE, target);
    }

    @Assembles("TDIFEQ")
    public static Opcode TDIFEQ(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new DCondImpl(false), dst, src, COND_EQ, target);
    }

    @Assembles("TDIFNE")
    public static Opcode TDIFNE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new DCondImpl(false), dst, src, COND_NE, target);
    }

    @Assembles("TDIFGE")
    public static Opcode TDIFGE(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new DCondImpl(false), dst, src, COND_GE, target);
    }

    @Assembles("TDIFGT")
    public static Opcode TDIFGT(@Register Integer dst, @Register Integer src, @Immediate Integer target){
        return new Opcode(new DCondImpl(false), dst, src, COND_GT, target);
    }

    @Assembles("IP")
    public static Opcode IP(@Register Integer dst) {
        return new Opcode(new IpImpl(), dst);
    }

    @Assembles("STPTR")
    public static Opcode STPTR(@Register Integer dst, @Register Integer src){
        return new Opcode(new StPtrImpl(), dst, src);
    }

    @Assembles("STWRITE")
    public static Opcode STWRITE(@Register Integer dst, @Register Integer src, @Immediate Integer count){
        return new Opcode(new StWriteImpl(), dst, src, count);
    }

    @Assembles("STREAD")
    public static Opcode STREAD(@Register Integer dst, @Register Integer src, @Immediate Integer count){
        return new Opcode(new StReadImpl(), dst, src, count);
    }

    @Assembles("CALL0")
    public static Opcode CALL0(@Register Integer dst, @Immediate Integer symbol) {
        return new Opcode(new CallImpl(), dst, symbol);
    }

    @Assembles("CALL")
    public static Opcode CALL(@Register Integer dst, @Immediate Integer symbol, @Register Integer src, @Immediate Integer count) {
        return new Opcode(new CallImpl(), dst, symbol, src, count);
    }

    @Assembles("DCALL0")
    public static Opcode DCALL0(@Register Integer dst, @Immediate Integer symbol) {
        return new Opcode(new DCallImpl(), dst, symbol);
    }

    @Assembles("DCALL")
    public static Opcode DCALL(@Register Integer dst, @Immediate Integer symbol, @Register Integer src, @Immediate Integer count) {
        return new Opcode(new DCallImpl(), dst, symbol, src, count);
    }

    @Assembles("FCALL0")
    public static Opcode FCALL0(@Immediate Integer symbol) {
        return new Opcode(new FCallImpl(), symbol);
    }

    @Assembles("FCALL")
    public static Opcode FCALL(@Register Integer dst, @Immediate Integer symbol, @Register Integer src, @Immediate Integer count) {
        return new Opcode(new FCallImpl(), dst, symbol, src, count);
    }

    @Assembles("DFCALL0")
    public static Opcode DFCALL0(@Immediate Integer symbol) {
        return new Opcode(new DFCallImpl(), symbol);
    }

    @Assembles("DFCALL")
    public static Opcode DFCALL(@Register Integer dst, @Immediate Integer symbol, @Register Integer src, @Immediate Integer count) {
        return new Opcode(new DFCallImpl(), dst, symbol, src, count);
    }

    @Assembles("SPAWN")
    public static Opcode SPAWN(@Register Integer dst, @Immediate Integer src){
        return new Opcode(new SpawnImpl(), dst, src);
    }

    @Assembles("YIELD")
    public static Opcode YIELD(){
        return new Opcode(new YieldImpl());
    }

    @Assembles("BLOCK")
    public static Opcode BLOCK(){
        return new Opcode(new BlockImpl());
    }

    @Assembles("UNBLOCK")
    public static Opcode UNBLOCK(@Register Integer dst){
        return new Opcode(new UnblockImpl(), dst);
    }

    @Assembles("WRITE")
    public static Opcode WRITE(@Register Integer dst, @Register Integer src, @Immediate Integer size) {
        return new Opcode(new WriteImpl(), dst, src, size);
    }

    @Assembles("READ")
    public static Opcode READ(@Register Integer dst, @Register Integer src, @Immediate Integer size) {
        return new Opcode(new ReadImpl(), dst, src, size);
    }

    @Assembles("FWRITE")
    public static Opcode FWRITE(@Register Integer dst, @Register Integer src, @Immediate Integer size, @Immediate Integer offset) {
        return new Opcode(new WriteImpl(), dst, src, size, offset);
    }

    @Assembles("FREAD")
    public static Opcode FREAD(@Register Integer dst, @Register Integer src, @Immediate Integer size, @Immediate Integer offset) {
        return new Opcode(new ReadImpl(), dst, src, size, offset);
    }

    @Assembles("MEM_COPY")
    public static Opcode MEM_COPY(@Register Integer dst, @Register Integer src, @Register Integer end) {
        return new Opcode(new MemCopyImpl(), dst, src, end);
    }

    @Assembles("OBJ_COPY")
    public static Opcode OBJ_COPY(@Register Integer dst, @Register Integer src, @Register Integer end) {
        return new Opcode(new ObjCopyImpl(), dst, src, end);
    }

    @Assembles("ALLOCATED")
    public static Opcode ALLOCATED(@Register Integer dst) {
        return new Opcode(new AllocatedImpl(), dst);
    }

    @Assembles("OBJ_ALLOCATED")
    public static Opcode OBJ_ALLOCATED(@Register Integer dst) {
        return new Opcode(new ObjAllocatedImpl(), dst);
    }

    @Assembles("RESIZE")
    public static Opcode RESIZE(@Register Integer dst, @Register Integer size) {
        return new Opcode(new ResizeImpl(), dst, size);
    }

    @Assembles("OBJ_RESIZE")
    public static Opcode OBJ_RESIZE(@Register Integer dst, @Register Integer size) {
        return new Opcode(new ObjResizeImpl(), dst, size);
    }

    @Assembles("STR")
    public static Opcode STR(@Register Integer dst, @Register Integer src, @Immediate Integer type) {
        return new Opcode(new StrImpl(), dst, src, type);
    }

    @Assembles("STR_LEN")
    public static Opcode STR_LEN(@Register Integer dst, @Register Integer src) {
        return new Opcode(new StrLenImpl(), dst, src);
    }

    @Assembles("CHAR_AT")
    public static Opcode CHAR_AT(@Register Integer dst, @Register Integer src, @Register Integer index) {
        return new Opcode(new CharAtImpl(), dst, src, index);
    }

    @Assembles("TO_CHARS")
    public static Opcode TO_CHARS(@Register Integer dst, @Register Integer src) {
        return new Opcode(new ToCharsImpl(), dst, src);
    }

    @Assembles("FROM_CHARS")
    public static Opcode FROM_CHARS(@Register Integer dst, @Register Integer src) {
        return new Opcode(new FromCharsImpl(), dst, src);
    }

    @Assembles("CONCAT")
    public static Opcode CONCAT(@Register Integer dst, @Register Integer src) {
        return new Opcode(new ConcatImpl(), dst, src);
    }

    @Assembles("SUB_STR")
    public static Opcode SUB_STR(@Register Integer dst, @Register Integer src, @Register Integer start, @Register Integer end) {
        return new Opcode(new SubStrImpl(), dst, src, start, end);
    }

    @Assembles("SCOMP")
    public static Opcode SCOMP(@Register Integer dst, @Register Integer a, @Register Integer b) {
        return new Opcode(new SCompImpl(), dst, a, b);
    }

    @Assembles("TRAPS")
    public static Opcode TRAPS(@Immediate Integer tableAddr, @Immediate Integer handlerCount) {
        return new Opcode(new TrapsImpl(), tableAddr, handlerCount);
    }

    @Assembles("TRAP")
    public static Opcode TRAP(@Register Integer code) {
        return new Opcode(new TrapImpl(), code);
    }

    @Assembles("PARAMS")
    public static Opcode PARAMS(@Register Integer dst, @Immediate Integer pIndex, @Immediate Integer count){
        return new Opcode(new ParamsImpl(), dst, pIndex, count);
    }

    @Assembles("DEBUG")
    public static Opcode DEBUG(@Immediate Integer sentinel, @Register Integer src){
        return new Opcode(new DebugImpl(), sentinel, src);
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
