package myworld.hummingbird;

public record Symbol(String name, Type type, int offset, int rType, int pTypeFlags, int iCount, int fCount, int lCount, int dCount, int sCount, int oCount) {

    public static final int FOREIGN_FUNCTION_OFFSET = -1;

    public enum Type {
        DATA,
        FUNCTION
    }

    public boolean isForeignFunction(){
        return type == Type.FUNCTION && offset == FOREIGN_FUNCTION_OFFSET;
    }
}
