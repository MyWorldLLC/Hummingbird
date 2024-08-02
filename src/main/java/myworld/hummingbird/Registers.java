package myworld.hummingbird;

public record Registers(long[] reg) {

    public Registers(){
        this(new long[1]);
    }
}
