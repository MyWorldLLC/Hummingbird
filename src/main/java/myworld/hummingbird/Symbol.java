package myworld.hummingbird;

public record Symbol(String name, Type type, int offset, TypeFlag rType, Params parameters, TypeCounts registers) {

    public static final int FOREIGN_FUNCTION_OFFSET = -1;

    public enum Type {
        DATA,
        FUNCTION,
        FOREIGN
    }

    public static Symbol empty(String name){
        return new Symbol(name, null, FOREIGN_FUNCTION_OFFSET, null, Params.zeroes(), null);
    }

    public static Symbol data(String name, int offset){
        return new Symbol(name, Type.DATA, offset, null, null, null);
    }

    public static Symbol function(String name, int offset, TypeFlag rType, Params parameters, TypeCounts registers){
        return new Symbol(name, Type.FUNCTION, offset, rType, parameters, registers);
    }

    public static Symbol foreignFunction(String name, TypeFlag rType, Params parameters, TypeCounts registers){
        return new Symbol(name, Type.FUNCTION, FOREIGN_FUNCTION_OFFSET, rType, parameters, registers);
    }

    public boolean isForeignFunction(){
        return type == Type.FOREIGN;
    }
}
