package myworld.hummingbird;

import java.util.Arrays;

import static myworld.hummingbird.HummingbirdVM.*;

public class Opcodes {

    public static final int INT_T = 0;
    public static final int FLOAT_T = 1 << 24;
    public static final int LONG_T = 2 << 24;
    public static final int DOUBLE_T = 3 << 24;
    public static final int STRING_T = 4 << 24;
    public static final int OBJECT_T = 5 << 24;
    public static final int VOID_T = 6 << 24;

    public static final int[] opcodeTable = new int[256];


    // ========= CONST =========
    public static final int CONST = 0x00;

    // ========= Arithmetic =========
    public static final int ADD = 0x01;
    public static final int SUB = 0x02;
    public static final int MUL = 0x03;
    public static final int DIV = 0x04;
    public static final int POW = 0x05;

    // ========= Conditionals =========
    public static final int IFLT = 0x06;
    public static final int IFLE = 0x07;
    public static final int IFEQ = 0x08;
    public static final int IFGE = 0x09;
    public static final int IFGT = 0x0A;

    // ========= Returns =========
    public static final int RETURN = 0x0B;
    public static final int VRETURN = 0x0C;

    // ========= Calls =========
    public static final int CALL = 0x0D;
    public static final int VCALL = 0x0E;
    public static final int DCALL = 0x0F; // Dynamic calls - parameter counts in registers instead of immediates
    public static final int DVCALL = 0x10;

    // ========= Bitwise Ops =========
    public static final int BAND = 0x11;
    public static final int BOR = 0x12;
    public static final int BXOR = 0x13;
    public static final int BNOT = 0x14;
    public static final int BLSHIFT = 0x15;
    public static final int BSRSHIFT = 0x16;
    public static final int BURSHIFT = 0x17;

    // ========= Conversion Ops =========
    public static final int CONV = 0x18;

    // ========= Memory Ops =========
    public static final int WRITE = 0x19;
    public static final int READ = 0x1A;
    public static final int GWRITE = 0x1B;
    public static final int GREAD = 0x1C;

    // ========= String Ops =========
    public static final int CHAR_AT = 0x1D;
    public static final int STR = 0x1E;

    // ========= Frame Inspection Ops =========
    public static final int PARENT = 0x1F;
    public static final int REG_COUNT = 0x20;
    public static final int REG_COPY = 0x21;
    public static final int IP = 0x22;

    // ========= Fiber Ops =========
    public static final int YIELD = 0x23;

    static {
        Arrays.fill(opcodeTable, 0xFFFFFFFF);

        // ========= CONST =========
        opcodeTable[I_CONST] = INT_T | CONST;
        opcodeTable[F_CONST] = FLOAT_T | CONST;
        opcodeTable[L_CONST] = LONG_T | CONST;
        opcodeTable[D_CONST] = DOUBLE_T | CONST;
        opcodeTable[S_CONST] = STRING_T | CONST;

        // ========= Arithmetic =========
        opcodeTable[I_ADD] = INT_T | ADD;

        // ========= Conditionals =========
        opcodeTable[I_IFLT] = INT_T | IFLT;
        opcodeTable[I_RETURN] = INT_T | RETURN;
    }

    public static int opcodeEncoding(int type, int baseOpcode){
        var encoded = Opcode.encodeOpcodeType(type, baseOpcode);
        for(int i = 0; i < opcodeTable.length && opcodeTable[i] != 0xFFFFFFFF; i++){
            if(encoded == opcodeTable[i]){
                return i;
            }
        }
        return -1;
    }

    @Assembles("CONST")
    public static Opcode CONST(@Register Integer dst, @Immediate Integer src){
        var type = Opcode.registerType(dst);
        return new Opcode(opcodeEncoding(type, CONST), Opcode.registerIndex(dst), src);
    }

    @Assembles("ADD")
    public static Opcode ADD(@Register Integer dst, @Register Integer src){
        var type = Opcode.registerType(dst);
        return new Opcode(opcodeEncoding(type, ADD), Opcode.registerIndex(dst), src);
    }

    @Assembles("IFLT")
    public static Opcode IFLT(@Register Integer dst, @Register Integer src, @Register Integer target){
        var type = Opcode.registerType(dst);
        return new Opcode(opcodeEncoding(type, IFLT), Opcode.registerIndex(dst), src, target);
    }

    @Assembles("RETURN")
    public static Opcode RETURN(@Register Integer src){
        var type = Opcode.registerType(src);
        return new Opcode(opcodeEncoding(type, RETURN), src);
    }

}
