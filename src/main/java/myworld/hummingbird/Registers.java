package myworld.hummingbird;

public record Registers(int[] ireg, long[] lreg, float[] freg, double[] dreg, String[] sreg, Object[] oreg) {

    public Registers(){
        this(new int[1], new long[1], new float[1], new double[1], new String[1], new Object[1]);
    }
}
