package myworld.hummingbird.util;

import java.util.Arrays;

public class Struct {

    private final int[] offsets;
    private final int[] sizes;
    private final int sizeOf;

    public Struct(int[] offsets, int[] sizes){
        this.offsets = offsets;
        this.sizes = sizes;
        sizeOf = Arrays.stream(sizes).sum();
    }

    public int offsetOf(int field){
        return offsets[field];
    }

    public int sizeOf(int field){
        return sizes[field];
    }

    public int sizeOf(){
        return sizeOf;
    }

    public int pointerTo(int p, int field){
        return p + offsetOf(field);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        private int[] sizes = new int[5];
        private int nextField = 0;

        public int withField(int byteSize){
            verifySize();
            sizes[nextField] = byteSize;

            int field = nextField;
            nextField++;
            return field;
        }

        public Struct build(){

            int[] offsets = new int[sizes.length];
            for(int i = 1; i < offsets.length; i++){
                offsets[i] = offsets[i - 1] + sizes[i];
            }

            return new Struct(offsets, sizes);
        }

        private void verifySize(){
            if(nextField == sizes.length - 1){
                sizes = resize(sizes);
            }
        }

        private int[] resize(int[] i){
            var next = new int[i.length + 5];
            System.arraycopy(i, 0, next, 0, i.length);
            return next;
        }
    }

}
