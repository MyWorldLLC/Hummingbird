package myworld.hummingbird;

public record Symbol(String name, Type type, int offset, TypeFlag rType, Params parameters, Params registers) {

    public static final int EMPTY_OFFSET = -1;

    public enum Type {
        DATA,
        FUNCTION,
        FOREIGN
    }

    public static Symbol empty(String name){
        return new Symbol(name, null, EMPTY_OFFSET, null, Params.zeroes(), Params.zeroes());
    }

    public static Symbol data(String name, int offset){
        return new Symbol(name, Type.DATA, offset, null, null, null);
    }

    public static Symbol function(String name, int offset, TypeFlag rType, Params parameters, Params registers){
        return new Symbol(name, Type.FUNCTION, offset, rType, parameters, registers);
    }

    public static Symbol foreignFunction(String name, int foreignIndex, TypeFlag rType, Params parameters){
        return new Symbol(name, Type.FUNCTION, foreignIndex, rType, parameters, null);
    }

    public boolean isForeignFunction(){
        return type == Type.FOREIGN;
    }
}
