package myworld.hummingbird;

public record Opcode(int opcode, int dst, int src, int extra, int extra1, int extra2, int extra3, int extra4, int extra5){
    public Opcode(int opcode, int dst, int src){
        this(opcode, dst, src, 0);
    }

    public Opcode(int opcode, int dst, int src, int extra){
        this(opcode, dst, src, extra, 0, 0, 0, 0, 0);
    }

    public Opcode(int opcode, int dst){
        this(opcode, dst, 0, 0, 0, 0, 0, 0, 0);
    }

    public static int registerType(int reg){
        return (reg >> 24) & 0xFF;
    }

    public static int registerIndex(int reg){
        return 0x00FFFFFF & reg;
    }

    public static int encodeRegisterOperand(int type, int reg){
        return (type << 24) | reg;
    }

    public static int encodeOpcodeType(int type, int opcode){
        return type | opcode;
    }

}
