package myworld.hummingbird.util.gc;

import myworld.hummingbird.util.Sizes;
import myworld.hummingbird.util.Struct;

public class GcObject {

    protected final int sizeField;
    protected final int refCountField;
    protected final Struct struct;

    public GcObject(){
        var builder = Struct.builder();
        sizeField = builder.withField(Sizes.INT);
        refCountField = builder.withField(Sizes.INT);
        struct = builder.build();
    }

    public int sizeField() {
        return sizeField;
    }

    public int refCountField(){
        return refCountField;
    }

    public Struct struct() {
        return struct;
    }
}
