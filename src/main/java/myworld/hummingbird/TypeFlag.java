package myworld.hummingbird;

public enum TypeFlag {
    INT,
    FLOAT,
    LONG,
    DOUBLE,
    OBJECT,
    VOID;

    public static int flagBitFor(TypeFlag flag){
        return 0x01 << flag.ordinal();
    }

    public static int toFlags(Iterable<TypeFlag> flags){
        var bitfield = 0;
        for(var flag : flags){
            bitfield |= flagBitFor(flag);
        }
        return bitfield;
    }

    public static boolean isSet(int bits, TypeFlag flag){
        return (bits & flagBitFor(flag)) != 0;
    }

}
