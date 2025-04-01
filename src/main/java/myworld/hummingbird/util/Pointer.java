package myworld.hummingbird.util;

public class Pointer {

    public static String toString(int ptr){
        return "0x%02X".formatted(ptr);
    }
}
