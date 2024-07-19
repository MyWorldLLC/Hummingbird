package myworld.hummingbird;

public record TypeCounts(String name, int[] counts) {

    public TypeCounts{
        if(counts.length != TypeFlag.values().length - 1){
            throw new IllegalArgumentException("Invalid type count array - got "
                    + counts.length + " must be " + (TypeFlag.values().length - 1));
        }
    }

    public int intCounts(){
        return counts[TypeFlag.INT.ordinal()];
    }

    public int floatCounts(){
        return counts[TypeFlag.FLOAT.ordinal()];
    }

    public int longCounts(){
        return counts[TypeFlag.LONG.ordinal()];
    }

    public int doubleCounts(){
        return counts[TypeFlag.DOUBLE.ordinal()];
    }

    public int objectCounts(){
        return counts[TypeFlag.OBJECT.ordinal()];
    }

    public static int[] makeTypeCountArray(){
        return new int[TypeFlag.values().length - 1];
    }

    public Params toParams(){
        return new Params(
                intCounts(),
                floatCounts(),
                longCounts(),
                doubleCounts(),
                objectCounts()
        );
    }

}
