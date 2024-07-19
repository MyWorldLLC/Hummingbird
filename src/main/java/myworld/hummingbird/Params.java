package myworld.hummingbird;

public record Params(int iParam, int fParam, int lParam, int dParam, int oParam) {

    public static Params zeroes(){
        return new Params(0, 0, 0, 0, 0);
    }
}
