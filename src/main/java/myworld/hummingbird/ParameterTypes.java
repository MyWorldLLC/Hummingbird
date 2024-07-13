package myworld.hummingbird;

import java.util.ArrayList;

public record ParameterTypes(int iCount, int fCount, int lCount, int dCount, int sCount, int oCount) {

    public int getFlags(){
        var types = new ArrayList<TypeFlag>();
        if(iCount > 0) types.add(TypeFlag.INT);
        if(fCount > 0) types.add(TypeFlag.FLOAT);
        if(lCount > 0) types.add(TypeFlag.LONG);
        if(dCount > 0) types.add(TypeFlag.DOUBLE);
        if(sCount > 0) types.add(TypeFlag.STRING);
        if(oCount > 0) types.add(TypeFlag.OBJECT);

        return TypeFlag.toFlags(types);
    }
}
