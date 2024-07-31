package myworld.hummingbird;

public interface DebugHandler {

    void debug(HummingbirdVM vm, Fiber fiber, int staticValue, int dynamicValue);

}
