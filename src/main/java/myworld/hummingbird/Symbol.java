package myworld.hummingbird;

public record Symbol(String name, Type type, int offset, TypeFlag rType, int parameters, int registers) {

    public static final int EMPTY_OFFSET = -1;

    public enum Type {
        DATA,
        FUNCTION,
        FOREIGN
    }

    public static Symbol empty(String name){
        return new Symbol(name, null, EMPTY_OFFSET, null, 0, 0);
    }

    public static Symbol data(String name, int offset){
        return new Symbol(name, Type.DATA, offset, null, 0, 0);
    }

    public static Symbol function(String name, int offset, TypeFlag rType, int parameters, int registers){
        return new Symbol(name, Type.FUNCTION, offset, rType, parameters, registers);
    }

    public static Symbol foreignFunction(String name, int foreignIndex, TypeFlag rType, int parameters){
        return new Symbol(name, Type.FUNCTION, foreignIndex, rType, parameters, 0);
    }

    public boolean isForeignFunction(){
        return type == Type.FOREIGN;
    }
}
