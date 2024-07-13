package myworld.hummingbird.assembler;

public interface UnresolvedLabelHandler {
    void resolved(String label, int index) throws AssemblyException;
}
